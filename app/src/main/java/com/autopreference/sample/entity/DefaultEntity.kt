package com.autopreference.sample.entity

import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference

@SharedPreference(name = "default")
internal data class DefaultEntity(
    @PreferenceKey("id") private val id: Int,
    @PreferenceKey("name") private val name: String,
    @PreferenceKey("enabled") private val enabled: Boolean
)
