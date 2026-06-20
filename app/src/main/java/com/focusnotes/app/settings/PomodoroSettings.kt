package com.focusnotes.app.settings

import android.content.Context
import com.focusnotes.app.model.PomodoroPhase

/**
 * Mirrors the iOS `PomodoroSettings` (`@AppStorage`) — work/break lengths with
 * the same defaults (25 / 5 minutes), backed by SharedPreferences.
 */
class PomodoroSettings(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("focusnotes.settings", Context.MODE_PRIVATE)

    var workMinutes: Int
        get() = prefs.getInt(KEY_WORK, DEFAULT_WORK)
        set(value) = prefs.edit().putInt(KEY_WORK, value).apply()

    var breakMinutes: Int
        get() = prefs.getInt(KEY_BREAK, DEFAULT_BREAK)
        set(value) = prefs.edit().putInt(KEY_BREAK, value).apply()

    val workSeconds: Int get() = workMinutes * 60
    val breakSeconds: Int get() = breakMinutes * 60

    fun phaseDuration(phase: PomodoroPhase): Int = when (phase) {
        PomodoroPhase.WORK -> workSeconds
        PomodoroPhase.BREAK -> breakSeconds
    }

    companion object {
        private const val KEY_WORK = "pomodoroWorkMinutes"
        private const val KEY_BREAK = "pomodoroBreakMinutes"
        private const val DEFAULT_WORK = 25
        private const val DEFAULT_BREAK = 5
    }
}
