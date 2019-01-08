package ru.starksoft.autopreferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for annotate Entity class
 * <p>
 * name                     - name of SharedPreference and of generated file
 * defaultSharedPreferences - Use PreferenceManager.getDefaultSharedPreferences() or context.getSharedPreferences(name). true by default
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SharedPreference {
	String name();

	boolean defaultSharedPreferences() default true;

	boolean useCommit() default true;
}