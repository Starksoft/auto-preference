package com.autopreference.sample.entity;

import com.autopreferences.annotations.PreferenceKey;
import com.autopreferences.annotations.SharedPreference;

@SharedPreference(name = "debugSettings")
public final class DebugSettingsEntity {

	@PreferenceKey("serverUrl") private final String serverUrl;
	@PreferenceKey("chuckEnabled") private final boolean chuckEnabled;
	@PreferenceKey("stethoEnabled") private final boolean stethoEnabled;

	public DebugSettingsEntity(String serverUrl, boolean chuckEnabled, boolean stethoEnabled) {
		this.serverUrl = serverUrl;
		this.chuckEnabled = chuckEnabled;
		this.stethoEnabled = stethoEnabled;
	}

	@Override
	public String toString() {
		return "DebugSettingsEntity{" + "serverUrl='" + serverUrl + '\'' + ", chuckEnabled=" + chuckEnabled + ", stethoEnabled=" +
				stethoEnabled + '}';
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public boolean isChuckEnabled() {
		return chuckEnabled;
	}

	public boolean isStethoEnabled() {
		return stethoEnabled;
	}
}