package ru.starksoft.autopreferences.compiler

import java.util.*
import kotlin.reflect.KClass

internal enum class SupportedTypes(
		val defaultValue: String,
		val kotlinClass: KClass<*>,
		private val typeNameList: List<String>
) {

	INT("0", Int::class, Arrays.asList("int", Int::class.java.name)),
	STRING("\"\"", String::class, listOf(String::class.java.name)),
	BOOLEAN("false", Boolean::class, Arrays.asList("boolean", Boolean::class.java.name)),
	FLOAT("0f", Float::class, Arrays.asList("float", Float::class.java.name)),
	LONG("0L", Long::class, Arrays.asList("long", Long::class.java.name));

	fun getMethodName(get: Boolean): String {
		return (if (get) PREFIX_GET else PREFIX_PUT) + name.toLowerCase().camelCase()
	}

	companion object {

		@JvmStatic
		fun findByType(type: String): SupportedTypes? {
			val values = SupportedTypes.values()
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