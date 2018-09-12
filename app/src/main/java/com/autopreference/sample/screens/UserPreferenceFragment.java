package com.autopreference.sample.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public final class UserPreferenceFragment extends Fragment {

	public static final String TAG = "UserPreferenceFragment";

	@NonNull
	public static UserPreferenceFragment newInstance() {
		Bundle args = new Bundle();
		UserPreferenceFragment fragment = new UserPreferenceFragment();
		fragment.setArguments(args);
		return fragment;
	}


}