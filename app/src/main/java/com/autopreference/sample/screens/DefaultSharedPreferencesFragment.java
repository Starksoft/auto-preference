package com.autopreference.sample.screens;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.autopreference.sample.R;

public final class DefaultSharedPreferencesFragment extends PreferenceFragmentCompat {

	public static final String TAG = "DefaultSharedPreference";

	@NonNull
	public static DefaultSharedPreferencesFragment newInstance() {
		Bundle args = new Bundle();
		DefaultSharedPreferencesFragment fragment = new DefaultSharedPreferencesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		setPreferencesFromResource(R.xml.default_preferences, null);
	}
}