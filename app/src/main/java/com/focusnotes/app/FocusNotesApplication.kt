package com.focusnotes.app

import android.app.Application
import android.content.Context
import com.focusnotes.app.data.AppDatabase
import com.focusnotes.app.data.TaskRepository
import com.focusnotes.app.notification.NotificationService
import com.focusnotes.app.settings.PomodoroSettings
import com.focusnotes.app.util.Haptics

/** Tiny manual dependency container — the Android analog of the iOS singletons. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val repository = TaskRepository(database.taskDao())
    val settings = PomodoroSettings(context)
    val notifications = NotificationService(context)
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
