package ru.starksoft.autopreferences.compiler

import ru.starksoft.autopreferences.SharedPreference
import java.util.Locale
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal object Utils {

    @JvmStatic
    fun camelCase(input: String): String {
        return input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1, input.length)
    }

    @JvmStatic
    fun isNullOrEmpty(enclosedElements: List<Element?>?): Boolean = enclosedElements.isNullOrEmpty()

    @JvmStatic
    fun getPreferenceFullName(typeElement: TypeElement): String {
        return camelCase(typeElement.getAnnotation(SharedPreference::class.java).name) + "Preferences"
    }

}
