package ru.starksoft.autopreferences

/**
 * Used for annotate Entity class
 *
 *
 * name                     - name of SharedPreference and of generated file
 * defaultSharedPreferences - Use PreferenceManager.getDefaultSharedPreferences() or context.getSharedPreferences(name). true by default
 */
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class SharedPreference(val name: String, val defaultSharedPreferences: Boolean = true, val useCommit: Boolean = true)