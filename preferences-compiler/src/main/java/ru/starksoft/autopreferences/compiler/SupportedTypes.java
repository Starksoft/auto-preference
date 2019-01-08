package ru.starksoft.autopreferences.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.starksoft.autopreferences.compiler.Common.PREFIX_GET;
import static ru.starksoft.autopreferences.compiler.Common.PREFIX_PUT;

enum SupportedTypes {

	INT("0", Arrays.asList("int", Integer.class.getName())),
	STRING("\"\"", Collections.singletonList(String.class.getName())),
	BOOLEAN("false", Arrays.asList("boolean", Boolean.class.getName())),
	FLOAT("0f", Arrays.asList("float", Float.class.getName())),
	LONG("0l", Arrays.asList("long", Long.class.getName()));

	private final String defaultValue;
	private final List<String> typeNameList;

	SupportedTypes(String defaultValue, List<String> typeNameList) {
		this.defaultValue = defaultValue;
		this.typeNameList = typeNameList;
	}

	static SupportedTypes findByType(String type) {
		SupportedTypes[] values = SupportedTypes.values();
		for (SupportedTypes value : values) {

			List<String> typeNameList = value.typeNameList;
			for (String name : typeNameList) {
				if (type.equalsIgnoreCase(name)) {
					return value;
				}
			}
		}
		return null;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	String getMethodName(boolean get) {
		return (get ? PREFIX_GET : PREFIX_PUT) + Utils.camelCase(name().toLowerCase());
	}
}