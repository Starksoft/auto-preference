package com.autopreference.sample.di.module;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;

import ru.starksoft.autopreferences.build.AppPreferences;

public final class AppPreferencesProvider {

	private final Class<AppPreferences> data = AppPreferences.class;
	@NonNull private final Context context;

	private AppPreferences instance = null;

	public AppPreferencesProvider(@NonNull Context context) {
		this.context = context;
	}

	@NonNull
	public AppPreferences getData() {

		if (instance == null) {
			try {
				instance = data.getConstructor(Context.class).newInstance(context);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		return instance;
	}
}