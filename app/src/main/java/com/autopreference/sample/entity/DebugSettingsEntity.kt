package com.autopreference.sample.entity

import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference

@SharedPreference(name = "debugSettings")
internal data class DebugSettingsEntity(
    @PreferenceKey("serverUrl") val serverUrl: String,
    @PreferenceKey("chuckEnabled") val isChuckEnabled: Boolean,
    @PreferenceKey("stethoEnabled") val isStethoEnabled: Boolean
)
