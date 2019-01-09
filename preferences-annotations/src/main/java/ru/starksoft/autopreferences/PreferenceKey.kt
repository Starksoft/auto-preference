package ru.starksoft.autopreferences

/**
 * Marks field as SharedPreference Key that will be used in SharedPreference.putString("key", value)
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class PreferenceKey(val value: String, val generateDefaultOverloadMethod: Boolean = true)