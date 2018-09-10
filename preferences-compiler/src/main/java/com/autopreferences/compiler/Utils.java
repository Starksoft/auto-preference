package com.autopreferences.compiler;

import java.util.List;

import javax.lang.model.element.Element;

public final class Utils {

	private Utils() {
		throw new UnsupportedOperationException();
	}

	static String camelCase(String in) {
		return in.substring(0, 1).toUpperCase() + in.substring(1, in.length());
	}

	static boolean isNullOrEmpty(List<? extends Element> enclosedElements) {
		return enclosedElements == null || enclosedElements.isEmpty();
	}
}