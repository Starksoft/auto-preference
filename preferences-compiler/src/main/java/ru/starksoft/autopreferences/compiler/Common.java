package ru.starksoft.autopreferences.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

@SuppressWarnings("WeakerAccess")
public final class Common {

	static final String PREFIX_GET = "get";
	static final String PREFIX_IS = "is";
	static final ClassName SHARED_PREFERENCES_CLASS_NAME = ClassName.get("android.content", "SharedPreferences");
	static final ClassName NON_NULL_CLASS_NAME = ClassName.get("android.support.annotation", "NonNull");
	static final String PACKAGE_NAME = "ru.starksoft.autopreferences.build";
	static final String CLASS_NAME_APP_PREFERENCES = "AppPreferences";
	static final ClassName PREFERENCE_MANAGER_CLASS_NAME = ClassName.get("android.preference", "PreferenceManager");
	static final String PREFIX_CONTAINS = "contains";
	static final String PREFIX_REMOVE = "remove";
	static final String PREFIX_PUT = "put";

	private Common() {
		throw new UnsupportedOperationException();
	}

	static void printFatalMessage(ProcessingEnvironment processingEnvironment, String message) {
		processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "\n\n\n" + PreferencesProcessor.TAG + " " + message);
	}

	static void createContextConstructor(TypeSpec.Builder builder, boolean publicConstructor) {
		ClassName context = ClassName.get("android.content", "Context");
		builder.addField(context, "context", Modifier.PRIVATE, Modifier.FINAL);

		// Constructor
		MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
				.addParameter(ParameterSpec.builder(context, "context").addAnnotation(NON_NULL_CLASS_NAME).build())
				.addStatement("this.context = context");

		if (publicConstructor) {
			methodSpecBuilder.addModifiers(Modifier.PUBLIC);
		}

		builder.addMethod(methodSpecBuilder.build());
	}

	static void generateFile(ProcessingEnvironment processingEnvironment, TypeSpec typeSpec) throws IOException {
		JavaFile.builder(PACKAGE_NAME, typeSpec).build().writeTo(processingEnvironment.getFiler());
	}
}

