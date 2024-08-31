package com.autopreference.sample

import android.app.Application
import ru.starksoft.autopreferences.build.AppPreferences

internal class ApplicationInstance : Application() {

    lateinit var appPreferences: AppPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(applicationContext)
        instance = this
    }

    companion object {

        lateinit var instance: ApplicationInstance
            private set
    }
}
