package com.focusnotes.app

import android.app.Application
import android.content.Context
import com.focusnotes.app.data.AppDatabase
import com.focusnotes.app.data.TaskRepository
import com.focusnotes.app.notification.NotificationService
import com.focusnotes.app.notification.PomodoroAlarmScheduler
import com.focusnotes.app.settings.PomodoroSettings
import com.focusnotes.app.util.Haptics

/** Tiny manual dependency container — the Android analog of the iOS singletons. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val repository = TaskRepository(database.taskDao())
    val settings = PomodoroSettings(context)

    // Created at startup so the notification channel exists before any alarm fires.
    val notifications = NotificationService(context)
    val scheduler = PomodoroAlarmScheduler(context)
    val haptics = Haptics(context)
}

class FocusNotesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
