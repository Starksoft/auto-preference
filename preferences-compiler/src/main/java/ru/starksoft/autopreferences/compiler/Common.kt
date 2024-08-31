package ru.starksoft.autopreferences.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic

/*

 */
internal object Common {

    @JvmField
    val SHARED_PREFERENCES_CLASS_NAME: ClassName = ClassName.get("android.content", "SharedPreferences")

    @JvmField
    val NON_NULL_CLASS_NAME: ClassName = ClassName.get("androidx.annotation", "NonNull")

    @JvmField
    val PREFERENCE_MANAGER_CLASS_NAME: ClassName = ClassName.get("android.preference", "PreferenceManager")

    const val PACKAGE_NAME: String = "ru.starksoft.autopreferences.build"
    const val CLASS_NAME_APP_PREFERENCES: String = "AppPreferences"

    const val PREFIX_GET: String = "get"
    const val PREFIX_IS: String = "is"
    const val PREFIX_CONTAINS: String = "contains"
    const val PREFIX_REMOVE: String = "remove"
    const val PREFIX_PUT: String = "put"

    @JvmStatic
    fun printFatalMessage(processingEnvironment: ProcessingEnvironment, message: String) {
        processingEnvironment.messager.printMessage(
            Diagnostic.Kind.ERROR, """
            ${PreferencesProcessor.TAG} $message"""
        )
    }

    @JvmStatic
    fun createContextConstructor(builder: TypeSpec.Builder, publicConstructor: Boolean) {
        val context = ClassName.get("android.content", "Context")
        builder.addField(context, "context", Modifier.PRIVATE, Modifier.FINAL)

        // Constructor
        val methodSpecBuilder = MethodSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder(context, "context").addAnnotation(NON_NULL_CLASS_NAME).build())
            .addStatement("this.context = context")

        if (publicConstructor) {
            methodSpecBuilder.addModifiers(Modifier.PUBLIC)
        }

        builder.addMethod(methodSpecBuilder.build())
    }

    @JvmStatic
    @Throws(IOException::class)
    fun generateFile(processingEnvironment: ProcessingEnvironment, typeSpec: TypeSpec?) {
        JavaFile.builder(PACKAGE_NAME, typeSpec).build().writeTo(processingEnvironment.filer)
    }
}

