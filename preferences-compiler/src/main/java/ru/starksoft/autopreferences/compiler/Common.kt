package ru.starksoft.autopreferences.compiler

import com.squareup.kotlinpoet.*
import java.io.File
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

val NON_NULL_CLASS_NAME = ClassName("android.support.annotation", "NonNull")
internal const val PREFIX_GET = "get"
internal const val PREFIX_IS = "is"
val SHARED_PREFERENCES_CLASS_NAME = ClassName("android.content", "SharedPreferences")

internal const val PACKAGE_NAME = "ru.starksoft.autopreferences.build"
internal const val CLASS_NAME_APP_PREFERENCES = "AppPreferences"
internal val PREFERENCE_MANAGER_CLASS_NAME = ClassName("android.preference", "PreferenceManager")
internal const val PREFIX_CONTAINS = "contains"
internal const val PREFIX_REMOVE = "remove"
const val PREFIX_PUT = "put"
const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

@Throws(IOException::class)
fun ProcessingEnvironment.generateFile(fileSpec: FileSpec) {
	val kaptKotlinGeneratedDir = this.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
	fileSpec.writeTo(File(kaptKotlinGeneratedDir))
}

fun ProcessingEnvironment.printFatalMessage(message: String) {
	this.messager.printMessage(Diagnostic.Kind.ERROR, "\n\n\n$TAG $message")
}

fun TypeSpec.Builder.createContextConstructor(name: String, publicConstructor: Boolean) {
	val context = ClassName("android.content", "Context")

	//	if (publicConstructor) {
	//		methodSpecBuilder.addModifiers(KModifier.PUBLIC)
	//	}

	val flux = FunSpec.constructorBuilder()
			.addParameter("context", context)
			.build()

	val helloWorld = this
			.primaryConstructor(flux)
			.addProperty(PropertySpec.builder("context", context)
								 .initializer("context")
								 .addModifiers(KModifier.PRIVATE)
								 .build())

}