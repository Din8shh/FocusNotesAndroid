package com.focusnotes.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Posts the Pomodoro phase-end notification when its scheduled alarm fires. */
class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(PomodoroAlarmScheduler.EXTRA_TITLE) ?: "Focus Notes"
        val body = intent.getStringExtra(PomodoroAlarmScheduler.EXTRA_BODY).orEmpty()
        NotificationService(context.applicationContext).notifyPhase(title, body)
    }
}
