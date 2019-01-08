# Auto preference [![Build Status](https://app.bitrise.io/app/eaba82e915aca473/status.svg?token=3bXW-URr9pRcZaC4rhukkA)](https://app.bitrise.io/app/eaba82e915aca473)[ ![compiler](https://api.bintray.com/packages/edwardstark/android-maven/preferences-compiler/images/download.svg) ](https://bintray.com/edwardstark/android-maven/preferences-compiler/_latestVersion) [![annotations](https://api.bintray.com/packages/edwardstark/android-maven/preferences-annotations/images/download.svg "annotations")](https://bintray.com/edwardstark/android-maven/preferences-annotations/_latestVersion)

Auto generated wrapper over standard SharedPreferences based on `@SharedPreference` annotation.
On each filed with `@PreferenceKey("name")` annotation will be generated methods:

`getName()`

`getName(defaultValue)`

`containsName()`

`removeName()`

`putName(name)`

Also will be generated method `removeAll()` & `isEmpty()`


```java
  public void removeAll() {
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    editor.remove("default_id");
    editor.remove("default_name");
    editor.remove("default_enabled");
    editor.commit();
  }

  public boolean isEmpty() {
    SharedPreferences sharedPreferences = getSharedPreferences();
    return !sharedPreferences.contains("default_id") && !sharedPreferences.contains("default_name") && !sharedPreferences.contains("default_enabled") ;}
  

```


You can choose where to save SharedPreferences: DefaultSharedPreferences or separeate Preference file by setting `defaultSharedPreferences` to false for separate file or true to use DefaultSharedPreferences (default)

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
implementation 'ru.starksoft:preferences-annotations:X.X.X'
annotationProcessor 'ru.starksoft:preferences-compiler:X.X.X'
```
