package com.autopreference.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.autopreference.sample.screens.DefaultSharedPreferencesFragment;
import com.autopreference.sample.screens.UserPreferenceFragment;

public final class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	public void onOpenUserSharedPreferencesButtonClicked(@NonNull View view) {
		SingleFragmentActivity.start(this, UserPreferenceFragment.TAG);
	}

	public void onOpenDefaultSharedPreferencesButtonClicked(@NonNull View view) {
		SingleFragmentActivity.start(this, DefaultSharedPreferencesFragment.TAG);
	}
}