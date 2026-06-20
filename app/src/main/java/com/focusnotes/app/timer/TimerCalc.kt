package com.focusnotes.app.timer

import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.activeTimerMode
import com.focusnotes.app.data.pomodoroPhase
import com.focusnotes.app.model.PomodoroPhase
import com.focusnotes.app.model.TimerMode

/**
 * Pure, wall-clock based timer math — the Android equivalent of the display
 * helpers on the iOS `TimerEngine`. A task is "active" purely by virtue of
 * having a non-null `activeTimerStartedAt`, so this is the single source of truth.
 */
object TimerCalc {

    fun isActive(task: TaskEntity): Boolean = task.activeTimerStartedAt != null

    fun sessionElapsedSeconds(task: TaskEntity, now: Long): Int {
        val started = task.activeTimerStartedAt ?: return 0
        return ((now - started) / 1000L).toInt().coerceAtLeast(0)
    }

    fun totalDisplaySeconds(task: TaskEntity, now: Long): Int =
        task.totalElapsedSeconds + sessionElapsedSeconds(task, now)

    fun pomodoroRemainingSeconds(task: TaskEntity, now: Long, workSeconds: Int, breakSeconds: Int): Int {
        if (task.activeTimerMode != TimerMode.POMODORO) return workSeconds
        val phase = task.pomodoroPhase ?: return workSeconds
        val phaseStartedAt = task.pomodoroPhaseStartedAt ?: return workSeconds

        val duration = phaseDuration(phase, workSeconds, breakSeconds)
        val elapsed = ((now - phaseStartedAt) / 1000L).toInt().coerceAtLeast(0)
        return (duration - elapsed).coerceAtLeast(0)
    }

    fun pomodoroProgress(task: TaskEntity, now: Long, workSeconds: Int, breakSeconds: Int): Float {
        if (task.activeTimerMode != TimerMode.POMODORO) return 0f
        val phase = task.pomodoroPhase ?: return 0f
        val phaseStartedAt = task.pomodoroPhaseStartedAt ?: return 0f

        val duration = phaseDuration(phase, workSeconds, breakSeconds).toFloat()
        if (duration <= 0f) return 0f
        val elapsed = (now - phaseStartedAt) / 1000f
        return (elapsed / duration).coerceIn(0f, 1f)
    }

    fun phaseDuration(phase: PomodoroPhase, workSeconds: Int, breakSeconds: Int): Int = when (phase) {
        PomodoroPhase.WORK -> workSeconds
        PomodoroPhase.BREAK -> breakSeconds
    }
}
