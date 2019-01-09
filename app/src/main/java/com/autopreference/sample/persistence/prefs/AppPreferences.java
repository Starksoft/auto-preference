package com.autopreference.sample.persistence.prefs;

import com.autopreference.sample.persistence.prefs.entity.DebugSettingsEntity;
import com.autopreference.sample.persistence.prefs.entity.DefaultEntity;
import com.autopreference.sample.persistence.prefs.entity.UserEntity;
import com.autopreference.sample.persistence.prefs.entity.UserEntityK;

import ru.starksoft.autopreferences.ApplicationPreferences;
import ru.starksoft.autopreferences.ApplicationPreferencesMarker;

@ApplicationPreferences(entities = {DebugSettingsEntity.class, DefaultEntity.class, UserEntity.class, UserEntityK.class})
public class AppPreferences implements ApplicationPreferencesMarker {


}