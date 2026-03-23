package com.ultrawork.notes

import android.app.Application
import com.ultrawork.notes.di.DefaultServiceLocator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotesApp : Application() {

    companion object {
        lateinit var serviceLocator: DefaultServiceLocator
    }

    override fun onCreate() {
        super.onCreate()
        serviceLocator = DefaultServiceLocator(BuildConfig.API_BASE_URL)
    }
}
