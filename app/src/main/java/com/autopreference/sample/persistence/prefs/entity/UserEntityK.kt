package com.autopreference.sample.persistence.prefs.entity

import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference

@SharedPreference(name = "userKotlin")
data class UserEntityK(
		@field:PreferenceKey("id")
		val id: Int
)