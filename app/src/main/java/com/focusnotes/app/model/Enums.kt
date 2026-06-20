package com.focusnotes.app.model

/** Mirrors the iOS `TaskStatus` enum. */
enum class TaskStatus(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    companion object {
        fun fromRaw(raw: String?): TaskStatus =
            entries.firstOrNull { it.name == raw } ?: TODO
    }
}

/** Mirrors the iOS `TimerMode` enum. */
enum class TimerMode(val label: String) {
    ELAPSED("Elapsed"),
    POMODORO("Pomodoro");

    companion object {
        fun fromRaw(raw: String?): TimerMode? =
            entries.firstOrNull { it.name == raw }
    }
}

/** Mirrors the iOS `PomodoroPhase` enum. */
enum class PomodoroPhase(val label: String) {
    WORK("Focus"),
    BREAK("Break");

    companion object {
        fun fromRaw(raw: String?): PomodoroPhase? =
            entries.firstOrNull { it.name == raw }
    }
}
