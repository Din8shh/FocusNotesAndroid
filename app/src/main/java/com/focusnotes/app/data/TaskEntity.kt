package com.focusnotes.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusnotes.app.model.PomodoroPhase
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.model.TimerMode
import java.util.UUID

/**
 * Room equivalent of the iOS SwiftData `TaskItem` model. Timer state is stored
 * on the task itself (wall-clock timestamps), so an in-progress timer survives
 * process death exactly as on iOS.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val statusRaw: String = TaskStatus.TODO.name,
    val notes: String = "",
    val dueDate: Long? = null,
    val isFocused: Boolean = false,
    val totalElapsedSeconds: Int = 0,
    val activeTimerStartedAt: Long? = null,
    val activeTimerModeRaw: String? = null,
    val pomodoroPhaseRaw: String? = null,
    val pomodoroPhaseStartedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

val TaskEntity.status: TaskStatus
    get() = TaskStatus.fromRaw(statusRaw)

val TaskEntity.activeTimerMode: TimerMode?
    get() = TimerMode.fromRaw(activeTimerModeRaw)

val TaskEntity.pomodoroPhase: PomodoroPhase?
    get() = PomodoroPhase.fromRaw(pomodoroPhaseRaw)

val TaskEntity.isTimerRunning: Boolean
    get() = activeTimerStartedAt != null
