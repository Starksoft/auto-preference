package com.autopreference.sample;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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