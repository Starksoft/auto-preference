package com.autopreference.sample.entity

import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference

@SharedPreference(name = "user", defaultSharedPreferences = false)
internal data class UserEntity(
    @PreferenceKey("id") private val id: Int,
    @PreferenceKey("name") private val name: String,
    @PreferenceKey("enabled") private val enabled: Boolean,
    @PreferenceKey("createdAt") private val date: Long,
    @PreferenceKey("floatValue") private val floatField: Float
) {
//    private val idWrapped: Int? = null
//    private val invalidField: Any? = null
}
