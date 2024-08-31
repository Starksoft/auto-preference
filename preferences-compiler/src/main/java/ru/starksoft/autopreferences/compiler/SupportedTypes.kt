package ru.starksoft.autopreferences.compiler

import ru.starksoft.autopreferences.compiler.Utils.camelCase
import java.util.Locale

internal enum class SupportedTypes(@JvmField val defaultValue: String, private val typeNameList: List<String>) {

    INT("0", listOf("int", Int::class.java.name)),
    STRING("\"\"", listOf(String::class.java.name)),
    BOOLEAN("false", listOf("boolean", Boolean::class.java.name)),
    FLOAT("0f", listOf("float", Float::class.java.name)),
    LONG("0l", listOf("long", Long::class.java.name));

    fun getMethodName(get: Boolean): String {
        return (if (get) Common.PREFIX_GET else Common.PREFIX_PUT) + camelCase(name.lowercase(Locale.getDefault()))
    }

    companion object {
        @JvmStatic
        fun findByType(type: String): SupportedTypes? {
            val values = entries.toTypedArray()
            for (value in values) {
                val typeNameList = value.typeNameList
                for (name in typeNameList) {
                    if (type.equals(name, ignoreCase = true)) {
                        return value
                    }
                }
            }
            return null
        }
    }
}
