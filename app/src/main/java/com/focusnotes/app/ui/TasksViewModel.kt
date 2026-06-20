package com.focusnotes.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.focusnotes.app.FocusNotesApplication
import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.TaskRepository
import com.focusnotes.app.data.TimerSessionEntity
import com.focusnotes.app.data.activeTimerMode
import com.focusnotes.app.data.pomodoroPhase
import com.focusnotes.app.model.PomodoroPhase
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.model.TimerMode
import com.focusnotes.app.notification.NotificationService
import com.focusnotes.app.settings.PomodoroSettings
import com.focusnotes.app.timer.TimerCalc
import com.focusnotes.app.util.Haptics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository,
    private val settings: PomodoroSettings,
    private val notifications: NotificationService,
    private val haptics: Haptics,
) : ViewModel() {

    val workSeconds: Int get() = settings.workSeconds
    val breakSeconds: Int get() = settings.breakSeconds

    val tasks: StateFlow<List<TaskEntity>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now.asStateFlow()

    init {
        // One central ticker drives the per-second UI updates and advances the
        // Pomodoro phase when a block finishes — the equivalent of the iOS
        // TimelineView + handlePomodoroTick.
        viewModelScope.launch {
            while (isActive) {
                _now.value = System.currentTimeMillis()
                val running = tasks.value.firstOrNull { it.activeTimerStartedAt != null }
                if (running != null && running.activeTimerMode == TimerMode.POMODORO) {
                    maybeAdvancePomodoro(running)
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun observeTask(id: String): Flow<TaskEntity?> = repository.observeTask(id)

    fun observeSessions(id: String): Flow<List<TimerSessionEntity>> = repository.observeSessions(id)

    // --- Task CRUD -----------------------------------------------------------

    fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.upsert(TaskEntity(title = trimmed))
        }
    }

    /** Persist an edited copy of a task, bumping `updatedAt` like iOS `touch()`. */
    fun save(task: TaskEntity) {
        viewModelScope.launch {
            repository.upsert(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    // --- Granular field edits (used by the detail screen) --------------------

    private fun mutate(id: String, transform: (TaskEntity) -> TaskEntity) {
        viewModelScope.launch {
            val current = repository.getTask(id) ?: return@launch
            repository.upsert(transform(current).copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateTitle(id: String, value: String) = mutate(id) { it.copy(title = value) }

    fun updateNotes(id: String, value: String) = mutate(id) { it.copy(notes = value) }

    fun setStatus(id: String, status: TaskStatus) = mutate(id) {
        it.copy(
            statusRaw = status.name,
            isFocused = if (status == TaskStatus.DONE) false else it.isFocused
        )
    }

    fun setFocused(id: String, focused: Boolean) = mutate(id) { it.copy(isFocused = focused) }

    fun setDueDate(id: String, millis: Long?) = mutate(id) { it.copy(dueDate = millis) }

    fun deleteById(id: String) {
        viewModelScope.launch {
            val current = repository.getTask(id)
            if (current?.activeTimerStartedAt != null) pauseInternal(current.id)
            repository.deleteById(id)
        }
    }

    fun toggleDone(task: TaskEntity) {
        viewModelScope.launch {
            val current = repository.getTask(task.id) ?: return@launch
            val nowDone = current.statusRaw != TaskStatus.DONE.name
            val updated = current.copy(
                statusRaw = if (nowDone) TaskStatus.DONE.name else TaskStatus.TODO.name,
                isFocused = if (nowDone) false else current.isFocused,
                updatedAt = System.currentTimeMillis()
            )
            repository.upsert(updated)
            if (nowDone) haptics.success()
        }
    }

    fun toggleFocus(task: TaskEntity) {
        viewModelScope.launch {
            val current = repository.getTask(task.id) ?: return@launch
            repository.upsert(
                current.copy(
                    isFocused = !current.isFocused,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch {
            // Stop the timer first so we don't leave a dangling active reference.
            val current = repository.getTask(task.id)
            if (current?.activeTimerStartedAt != null) {
                pauseInternal(current.id)
            }
            repository.deleteById(task.id)
        }
    }

    // --- Timer engine --------------------------------------------------------

    fun toggleTimer(task: TaskEntity, mode: TimerMode) {
        if (TimerCalc.isActive(task)) {
            viewModelScope.launch { pauseInternal(task.id) }
        } else {
            startTimer(task, mode)
        }
    }

    fun startTimer(task: TaskEntity, mode: TimerMode) {
        viewModelScope.launch {
            // Pause any other running task (single active timer, like iOS).
            tasks.value.firstOrNull { it.activeTimerStartedAt != null && it.id != task.id }
                ?.let { pauseInternal(it.id) }

            val fresh = repository.getTask(task.id) ?: return@launch
            if (fresh.activeTimerStartedAt != null) return@launch

            val now = System.currentTimeMillis()
            val updated = fresh.copy(
                activeTimerStartedAt = now,
                activeTimerModeRaw = mode.name,
                pomodoroPhaseRaw = if (mode == TimerMode.POMODORO) PomodoroPhase.WORK.name else null,
                pomodoroPhaseStartedAt = if (mode == TimerMode.POMODORO) now else null,
                updatedAt = now
            )
            repository.upsert(updated)
            haptics.medium()
        }
    }

    private suspend fun pauseInternal(taskId: String) {
        val task = repository.getTask(taskId) ?: return
        val startedAt = task.activeTimerStartedAt ?: return
        val now = System.currentTimeMillis()
        val elapsed = ((now - startedAt) / 1000L).toInt().coerceAtLeast(0)

        recordSession(task.id, task.activeTimerMode ?: TimerMode.ELAPSED, startedAt, now, elapsed)

        repository.upsert(
            task.copy(
                totalElapsedSeconds = task.totalElapsedSeconds + elapsed,
                activeTimerStartedAt = null,
                activeTimerModeRaw = null,
                pomodoroPhaseRaw = null,
                pomodoroPhaseStartedAt = null,
                updatedAt = now
            )
        )
        haptics.light()
    }

    private suspend fun maybeAdvancePomodoro(task: TaskEntity) {
        val remaining = TimerCalc.pomodoroRemainingSeconds(task, System.currentTimeMillis(), workSeconds, breakSeconds)
        if (remaining > 0) return

        val current = repository.getTask(task.id) ?: return
        if (current.activeTimerStartedAt == null || current.activeTimerMode != TimerMode.POMODORO) return
        val phase = current.pomodoroPhase ?: return
        val phaseStartedAt = current.pomodoroPhaseStartedAt ?: return

        val now = System.currentTimeMillis()
        val actualElapsed = ((now - phaseStartedAt) / 1000L).toInt().coerceAtLeast(0)

        if (phase == PomodoroPhase.WORK) {
            recordSession(current.id, TimerMode.POMODORO, phaseStartedAt, now, actualElapsed)
            repository.upsert(
                current.copy(
                    totalElapsedSeconds = current.totalElapsedSeconds + actualElapsed,
                    pomodoroPhaseRaw = PomodoroPhase.BREAK.name,
                    pomodoroPhaseStartedAt = now,
                    activeTimerStartedAt = now,
                    updatedAt = now
                )
            )
            notifications.notifyPhase(
                "Break time",
                "Take a short break before your next focus block."
            )
        } else {
            recordSession(current.id, TimerMode.POMODORO, phaseStartedAt, now, actualElapsed)
            repository.upsert(
                current.copy(
                    pomodoroPhaseRaw = PomodoroPhase.WORK.name,
                    pomodoroPhaseStartedAt = now,
                    activeTimerStartedAt = now,
                    updatedAt = now
                )
            )
            notifications.notifyPhase(
                "Focus block",
                "Time to get back to ${current.title}."
            )
        }
        haptics.success()
    }

    private suspend fun recordSession(
        taskId: String,
        mode: TimerMode,
        startedAt: Long,
        endedAt: Long,
        duration: Int,
    ) {
        if (duration <= 0) return
        repository.insertSession(
            TimerSessionEntity(
                taskId = taskId,
                modeRaw = mode.name,
                durationSeconds = duration,
                startedAt = startedAt,
                endedAt = endedAt
            )
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as FocusNotesApplication
                val container = app.container
                return TasksViewModel(
                    container.repository,
                    container.settings,
                    container.notifications,
                    container.haptics
                ) as T
            }
        }
    }
}
