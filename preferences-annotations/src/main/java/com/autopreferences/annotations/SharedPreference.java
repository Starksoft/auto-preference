package com.autopreferences.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for annotate Entity class
 *
 * @param name                     - name of SharedPreference and of generated file
 * @param defaultSharedPreferences - Use PreferenceManager.getDefaultSharedPreferences() or context.getSharedPreferences(name). true by default
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SharedPreference {
	String name();

	boolean defaultSharedPreferences() default true;
}