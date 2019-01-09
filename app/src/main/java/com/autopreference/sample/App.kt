package com.autopreference.sample

import android.app.Application
import com.autopreference.sample.di.component.AppComponent
import com.autopreference.sample.di.component.DaggerAppComponent
import com.autopreference.sample.di.module.AppModule

class App : Application() {

	private lateinit var appComponent: AppComponent

	override fun onCreate() {
		super.onCreate()
		instance = this
		createAppDaggerComponent()
	}

	private fun createAppDaggerComponent() {
		appComponent = createAppComponent()
		appComponent.inject(this)
	}

	private fun createAppComponent(): AppComponent {
		return DaggerAppComponent.builder().appModule(AppModule(this)).build()
	}

	companion object {

		private lateinit var instance: App

		@JvmStatic
		fun getInstance(): App {
			return instance
		}

		@JvmStatic
		fun getAppComponent(): AppComponent {
			return instance.appComponent
		}
	}
}
