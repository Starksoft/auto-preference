package com.autopreference.sample;

import android.app.Application;
import androidx.annotation.NonNull;

import ru.starksoft.autopreferences.build.AppPreferences;

public class ApplicationInstance extends Application {

	private static ApplicationInstance instance;
	private AppPreferences appPreferences;

	@NonNull
	public static ApplicationInstance getInstance() {
		return instance;
	}

	@NonNull
	public AppPreferences getAppPreferences() {
		return appPreferences;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		appPreferences = new AppPreferences(getApplicationContext());
		instance = this;
	}
}