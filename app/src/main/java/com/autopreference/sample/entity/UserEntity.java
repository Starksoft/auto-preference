package com.autopreference.sample.entity;

import android.support.annotation.NonNull;

import com.autopreferences.annotations.PreferenceKey;
import com.autopreferences.annotations.SharedPreference;

@SharedPreference(name = "user")
public final class UserEntity {

	@PreferenceKey("id") private final int id;
	@PreferenceKey("name") @NonNull private final String name;
	@PreferenceKey("enabled") private final boolean enabled;
	@PreferenceKey("createdAt") private final long date;
	@PreferenceKey("floatValue") private final float floatField;
	//	private Integer idWrapped;
	//	private Object invalidField;

	public UserEntity(int id, @NonNull String name, boolean enabled, long date, float floatField) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.date = date;
		this.floatField = floatField;
	}

	@Override
	public String toString() {
		return "UserEntity{" + "id=" + id + ", name='" + name + '\'' + ", enabled=" + enabled + ", date=" + date + ", floatField=" +
				floatField + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		UserEntity that = (UserEntity) o;

		if (id != that.id) {
			return false;
		}
		if (enabled != that.enabled) {
			return false;
		}
		if (date != that.date) {
			return false;
		}
		if (Float.compare(that.floatField, floatField) != 0) {
			return false;
		}
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + name.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		result = 31 * result + (int) (date ^ (date >>> 32));
		result = 31 * result + (floatField != +0.0f ? Float.floatToIntBits(floatField) : 0);
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

	public long getDate() {
		return date;
	}

	public float getFloatField() {
		return floatField;
	}
}
