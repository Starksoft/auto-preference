# auto-preference
======

Current version 1.0.5 [![Build Status](https://travis-ci.org/Starksoft/auto-preference.svg?branch=master)](https://travis-ci.org/Starksoft/auto-preference)
--------

Auto generated SharedPreferences based on @SharedPreference annotation.

You can choose where to save SharedPreferences: DefaultSharedPreferences or separeate Preference file

Usage
--------
Create Entity:

```java
      
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
```

AppPreferences will be generated, based on Entity
--------

```java
      AppPreferences appPreferences = new AppPreferences(context);
      DefaultPreferences defaultPreferences = appPreferences.getDefaultPreferences();
      // Returns immutable entity with values
      DefaultEntity defaultEntity = defaultPreferences.getDefaultEntity();
      
      int id = defaultPreferences.getId();
      int id2 = defaultPreferences.getId(-1);
      
      defaultPreferences.putId(1);
      defaultPreferences.removeId();
      defaultPreferences.containsId();
      
      // Remove all
      defaultPreferences.removeAll();
```

Download
--------
Gradle:
```groovy
implementation 'com.autopreference:preferences-annotations:X.X.X'
annotationProcessor 'com.autopreference:preferences-compiler:X.X.X'
```
