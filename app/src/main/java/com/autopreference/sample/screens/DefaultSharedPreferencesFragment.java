package com.autopreference.sample.screens;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public final class DefaultSharedPreferencesFragment extends PreferenceFragment {

	public static final String TAG = "DefaultSharedPreference";

	public static DefaultSharedPreferencesFragment newInstance() {
		Bundle args = new Bundle();
		DefaultSharedPreferencesFragment fragment = new DefaultSharedPreferencesFragment();
		fragment.setArguments(args);
		return fragment;
	}
}