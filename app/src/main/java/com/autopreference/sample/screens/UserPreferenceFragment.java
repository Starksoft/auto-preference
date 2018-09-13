package com.autopreference.sample.screens;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.autopreference.sample.ApplicationInstance;
import com.autopreference.sample.R;
import com.autopreferences.build.UserPreferences;

public final class UserPreferenceFragment extends Fragment {

	public static final String TAG = "UserPreferenceFragment";
	private final UserPreferences userPreferences = ApplicationInstance.getInstance().getAppPreferences().getUserPreferences();

	@NonNull
	public static UserPreferenceFragment newInstance() {
		Bundle args = new Bundle();
		UserPreferenceFragment fragment = new UserPreferenceFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_user_preference, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		CheckBox userEnabled = view.findViewById(R.id.userEnabled);
		userEnabled.setChecked(userPreferences.isEnabled());
		userEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				userPreferences.putEnabled(isChecked);
			}
		});

		EditText userName = view.findViewById(R.id.userName);
		userName.setText(userPreferences.getName());
		userName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				userPreferences.putName(s.toString());
			}
		});

		EditText userId = view.findViewById(R.id.userId);
		userId.setText(String.valueOf(userPreferences.getId()));
		userId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String toString = s.toString();
				if (toString.isEmpty()) {
					userPreferences.removeId();
				} else {
					userPreferences.putId(Integer.parseInt(toString));
				}
			}
		});
	}
}