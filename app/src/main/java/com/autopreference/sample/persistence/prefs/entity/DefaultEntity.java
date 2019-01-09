package com.autopreference.sample.persistence.prefs.entity;

import android.support.annotation.NonNull;

import ru.starksoft.autopreferences.PreferenceKey;
import ru.starksoft.autopreferences.SharedPreference;

@SharedPreference(name = "default")
public final class DefaultEntity {

	@PreferenceKey("id") private final int id;
	@PreferenceKey("name") @NonNull private final String name;
	@PreferenceKey("enabled") private final boolean enabled;

	public DefaultEntity(int id, @NonNull String name, boolean enabled) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "DefaultEntity{" + "id=" + id + ", name='" + name + '\'' + ", enabled=" + enabled + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultEntity that = (DefaultEntity) o;

		if (id != that.id) {
			return false;
		}
		if (enabled != that.enabled) {
			return false;
		}
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + name.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		return result;
	}

	public int getId() {

		return id;
	}

	@NonNull
	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
