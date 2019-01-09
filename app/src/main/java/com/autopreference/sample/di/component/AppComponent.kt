package com.autopreference.sample.di.component

import com.autopreference.sample.App
import com.autopreference.sample.di.module.AppModule
import com.autopreference.sample.persistence.prefs.AppPreferences
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

	fun inject(application: App)

	fun getAppPreferences(): AppPreferences
}