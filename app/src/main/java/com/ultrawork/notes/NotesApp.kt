package com.ultrawork.notes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация lazy-свойств ServiceLocator
        ServiceLocator.notesRepository
    }
}
