package ru.starksoft.autopreferences.compiler

import ru.starksoft.autopreferences.SharedPreference
import javax.lang.model.element.TypeElement

fun String.camelCase(): String {
	return this.substring(0, 1).toUpperCase() + this.substring(1, this.length)
}

fun TypeElement.getPreferenceFullName(): String {
	return this.getAnnotation(SharedPreference::class.java).name.camelCase() + "Preferences"
}