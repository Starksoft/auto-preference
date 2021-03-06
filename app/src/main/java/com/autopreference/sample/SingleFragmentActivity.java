package com.autopreference.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.autopreference.sample.screens.DefaultSharedPreferencesFragment;
import com.autopreference.sample.screens.UserPreferenceFragment;

public class SingleFragmentActivity extends AppCompatActivity {

	public static final String SCREEN_KEY = "screenKey";

	public static void start(Context context, String fragment) {
		Intent intent = new Intent(context, SingleFragmentActivity.class);

		intent.putExtra(SCREEN_KEY, fragment);

		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_fragment);

		String screenKey = getIntent().getExtras().getString(SCREEN_KEY);

		if (screenKey != null) {
			openFragment(screenKey);
		}
	}

	private void openFragment(String screenKey) {

		Fragment fragment = null;

		switch (screenKey) {

			case DefaultSharedPreferencesFragment.TAG:
				fragment = DefaultSharedPreferencesFragment.newInstance();
				break;

			case UserPreferenceFragment.TAG:
				fragment = UserPreferenceFragment.newInstance();
				break;
		}

		if (fragment != null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
		}
	}
}
