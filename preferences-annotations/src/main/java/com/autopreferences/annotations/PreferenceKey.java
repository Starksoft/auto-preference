package com.autopreferences.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks field as SharedPreference Key that will be used in SharedPreference.putString("key", value)
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PreferenceKey {
	String value();

	boolean generateDefaultOverloadMethod() default true;
}