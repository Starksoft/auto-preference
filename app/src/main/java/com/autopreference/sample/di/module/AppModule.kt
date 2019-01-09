package com.autopreference.sample.di.module

import android.content.Context
import com.autopreference.sample.App
import dagger.Module
import dagger.Provides
import ru.starksoft.autopreferences.build.AppPreferences
import javax.inject.Singleton

@Module
class AppModule(val application: App) {

	@Singleton
	@Provides
	fun providesApplication(): App {
		return application
	}

	@Singleton
	@Provides
	fun providesContext(): Context {
		return application.baseContext
	}

	@Singleton
	@Provides
	fun providesAppPreference(context: Context): AppPreferences {
		return AppPreferences(context)
	}

}