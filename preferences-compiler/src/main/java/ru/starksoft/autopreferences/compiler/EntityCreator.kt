package ru.starksoft.autopreferences.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference
import ru.starksoft.autopreferences.compiler.Common.NON_NULL_CLASS_NAME
import ru.starksoft.autopreferences.compiler.Common.PREFIX_GET
import ru.starksoft.autopreferences.compiler.Common.PREFIX_IS
import ru.starksoft.autopreferences.compiler.Common.createContextConstructor
import ru.starksoft.autopreferences.compiler.Common.generateFile
import ru.starksoft.autopreferences.compiler.Common.printFatalMessage
import ru.starksoft.autopreferences.compiler.SupportedTypes.Companion.findByType
import ru.starksoft.autopreferences.compiler.Utils.camelCase
import ru.starksoft.autopreferences.compiler.Utils.getPreferenceFullName
import ru.starksoft.autopreferences.compiler.Utils.isNullOrEmpty
import java.io.IOException
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

internal class EntityCreator(private val preferencesProcessor: PreferencesProcessor) {

    fun createEntityFile(typeElement: TypeElement) {
        val typeElementAnnotation = typeElement.getAnnotation(SharedPreference::class.java)
        val fileName = typeElementAnnotation.name
        val defaultSharedPreferences = typeElementAnnotation.defaultSharedPreferences
        val useCommit = typeElementAnnotation.useCommit

        val preferenceFullName = getPreferenceFullName(typeElement)
        val builder = TypeSpec.classBuilder(preferenceFullName).addModifiers(Modifier.FINAL, Modifier.PUBLIC)

        // Entity
        val name = typeElement.simpleName.toString()
        val entity = ClassName.get(typeElement)

        builder.addField(Common.SHARED_PREFERENCES_CLASS_NAME, "sharedPreferences", Modifier.PRIVATE)
        builder.addField(
            FieldSpec.builder(String::class.java, "TAG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"" + preferenceFullName + "\"")
                .build()
        )

        // Constructor
        createContextConstructor(builder, false)

        // Fields
        val enclosedElements = typeElement.enclosedElements

        check(!isNullOrEmpty(enclosedElements)) { "We need at least one field" }

        // getSharedPreferences() lazy-init method
        createGetSharedPreferencesMethod(fileName, defaultSharedPreferences, builder)

        // Entity creation
        createEntityMethod(builder, typeElement, name, entity)

        // Entity saving
        createEntitySaveMethod(builder, typeElement, name, entity)

        val supportedFields: MutableList<String> = ArrayList()

        for (enclosedElement in enclosedElements) {
            if (enclosedElement!!.kind == ElementKind.FIELD) {
                val preferenceKeyAnnotation = enclosedElement.getAnnotation(PreferenceKey::class.java)
                if (preferenceKeyAnnotation == null) {
                    printFatalMessage(
                        preferencesProcessor.processingEnvironment,
                        "Annotation @PreferenceKey not found on field$enclosedElement, file: $name"
                    )
                    continue
                }

                val modifiers = enclosedElement.modifiers

                if (!modifiers.contains(Modifier.FINAL)) {
                    printFatalMessage(
                        preferencesProcessor.processingEnvironment,
                        "Only final fields supported. Current field: $enclosedElement, file: $name"
                    )
                    continue
                }

                val classType = ClassName.get(enclosedElement.asType())
                val fieldClassName = classType.toString()
                val supportedType = findByType(fieldClassName)

                if (supportedType == null) {
                    printFatalMessage(preferencesProcessor.processingEnvironment, "Unsupported type: $fieldClassName")
                    continue
                }

                val annotationKey = preferenceKeyAnnotation.value
                val generateDefaultOverloadMethod = preferenceKeyAnnotation.generateDefaultOverloadMethod
                val preferenceRealKey = getPreferenceRealKey(fileName, annotationKey)

                supportedFields.add(preferenceRealKey)

                createEntityPutMethod(builder, classType, annotationKey, preferenceRealKey, supportedType, useCommit)

                createEntityGetMethod(builder, classType, annotationKey, preferenceRealKey, supportedType, generateDefaultOverloadMethod)

                createEntityRemoveMethod(builder, annotationKey, preferenceRealKey, useCommit)

                createEntityContainsMethod(builder, annotationKey, preferenceRealKey)
            }
        }

        // Clear preference
        createRemoveAllMethod(builder, supportedFields, defaultSharedPreferences, useCommit)

        // isEmpty
        createIsEmptyMethod(builder, supportedFields, defaultSharedPreferences)

        try {
            generateFile(preferencesProcessor.processingEnvironment, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createEntityRemoveMethod(builder: TypeSpec.Builder, annotationKey: String, preferenceRealKey: String, useCommit: Boolean) {
        builder.addMethod(
            MethodSpec.methodBuilder(Common.PREFIX_REMOVE + camelCase(annotationKey))
                .addModifiers(Modifier.PUBLIC)
                .addStatement(
                    "getSharedPreferences().edit().remove(\$S).\$L()",
                    preferenceRealKey,
                    (if (useCommit) "commit" else "apply")
                )
                .build()
        )
    }

    private fun createEntityGetMethod(
        builder: TypeSpec.Builder, classType: TypeName, annotationKey: String, preferenceRealKey: String,
        supportedType: SupportedTypes, generateDefaultOverloadMethod: Boolean
    ) {
        val name: String = (if (supportedType == SupportedTypes.BOOLEAN) PREFIX_IS else PREFIX_GET) + camelCase(annotationKey)
        val methodSpecBuilder = MethodSpec.methodBuilder(name).returns(classType).addModifiers(Modifier.PUBLIC).addStatement(
            "return getSharedPreferences().\$L(\$S, \$L)",
            supportedType.getMethodName(true),
            preferenceRealKey,
            supportedType.defaultValue
        )

        if (supportedType == SupportedTypes.STRING) {
            methodSpecBuilder.addAnnotation(NON_NULL_CLASS_NAME)
        }

        builder.addMethod(methodSpecBuilder.build())

        // Method with default value as argument
        if (generateDefaultOverloadMethod) {
            val defaultMethodSpecBuilder = MethodSpec.methodBuilder(name)
                .returns(classType)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(classType, "defaultValue").build())
                .addStatement(
                    "return getSharedPreferences().\$L(\$S, defaultValue)",
                    supportedType.getMethodName(true),
                    preferenceRealKey
                )

            if (supportedType == SupportedTypes.STRING) {
                methodSpecBuilder.addAnnotation(NON_NULL_CLASS_NAME)
            }

            builder.addMethod(defaultMethodSpecBuilder.build())
        }
    }

    private fun createEntityPutMethod(
        builder: TypeSpec.Builder, classType: TypeName, annotationKey: String, preferenceRealKey: String,
        supportedType: SupportedTypes, useCommit: Boolean
    ) {
        builder.addMethod(
            MethodSpec.methodBuilder(Common.PREFIX_PUT + camelCase(annotationKey))
                .returns(TypeName.VOID)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(classType, "value").build())
                .addStatement(
                    "getSharedPreferences().edit()." + supportedType.getMethodName(false) + "(\$S, value).\$L()",
                    preferenceRealKey,
                    (if (useCommit) "commit" else "apply")
                )
                .build()
        )
    }

    private fun getPreferenceRealKey(entityName: String, key: String): String {
        return entityName + "_" + key
    }

    private fun createGetSharedPreferencesMethod(fileName: String, defaultSharedPreferences: Boolean, builder: TypeSpec.Builder) {
        val codeBlockBuilder = CodeBlock.builder().add("if (sharedPreferences == null) {\n")

        if (defaultSharedPreferences) {
            codeBlockBuilder.addStatement(
                "\tsharedPreferences = \$T.getDefaultSharedPreferences(context)",
                Common.PREFERENCE_MANAGER_CLASS_NAME
            )
        } else {
            codeBlockBuilder.addStatement("\tsharedPreferences = context.getSharedPreferences(\$S, Context.MODE_PRIVATE)", fileName)
        }

        codeBlockBuilder.add("}\n").addStatement("return sharedPreferences")

        builder.addMethod(
            MethodSpec.methodBuilder("getSharedPreferences")
                .addAnnotation(NON_NULL_CLASS_NAME)
                .returns(Common.SHARED_PREFERENCES_CLASS_NAME)
                .addModifiers(Modifier.PRIVATE)
                .addCode(codeBlockBuilder.build())
                .build()
        )
    }

    private fun createEntityMethod(builder: TypeSpec.Builder, typeElement: TypeElement, name: String, entity: ClassName) {
        val enclosedElements = typeElement.enclosedElements

        var entityConstructor: Element? = null
        for (enclosedElement in enclosedElements) {
            if (enclosedElement.kind == ElementKind.CONSTRUCTOR) {
                val parameters = (enclosedElement as ExecutableElement).parameters

                if (!parameters.isEmpty()) {
                    entityConstructor = enclosedElement
                    break
                }
            }
        }

        if (entityConstructor == null) {
            printFatalMessage(preferencesProcessor.processingEnvironment, "Constructor not found")
            return
        }

        val paramsBuilder = StringBuilder()

        val parameters = (entityConstructor as ExecutableElement).parameters

        for (i in parameters.indices) {
            val parameter = parameters[i]

            val parameterSimpleName = parameter.simpleName

            for (enclosedElement in enclosedElements) {
                if (enclosedElement.kind == ElementKind.FIELD) {
                    if (enclosedElement.simpleName == parameterSimpleName) {
                        val annotation = enclosedElement.getAnnotation(PreferenceKey::class.java)

                        val isBoolean = enclosedElement.asType().toString().equals("boolean", ignoreCase = true)

                        paramsBuilder.append(if (isBoolean) PREFIX_IS else PREFIX_GET)
                            .append(camelCase(annotation.value))
                            .append("()")
                            .append(if (i == parameters.size - 1) "" else ", ")
                    }
                }
            }
        }
        val params = paramsBuilder.toString()

        if (params.isEmpty()) {
            printFatalMessage(
                preferencesProcessor.processingEnvironment,
                "Cant find any parameters, PreferencesProcessor will work properly if constructor parameters and field name are the same"
            )
            return
        }

        builder.addMethod(
            MethodSpec.methodBuilder(PREFIX_GET + name)
                .returns(entity)
                .addAnnotation(NON_NULL_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new \$T(\$L)", entity, params)
                .build()
        )
    }

    private fun createRemoveAllMethod(
        builder: TypeSpec.Builder, supportedFields: List<String>, defaultSharedPreferences: Boolean,
        useCommit: Boolean
    ) {
        if (defaultSharedPreferences) {
            val methodSpecBuilder = MethodSpec.methodBuilder(Common.PREFIX_REMOVE + "All")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("SharedPreferences.Editor editor = getSharedPreferences().edit()")

            for (supportedField in supportedFields) {
                methodSpecBuilder.addStatement("editor.remove(\$S)", supportedField)
            }

            methodSpecBuilder.addStatement("editor.\$L()", (if (useCommit) "commit" else "apply"))
            builder.addMethod(methodSpecBuilder.build())
        } else {
            builder.addMethod(
                MethodSpec.methodBuilder(Common.PREFIX_REMOVE + "All")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("getSharedPreferences().edit().clear().\$L()", (if (useCommit) "commit" else "apply"))
                    .build()
            )
        }
    }

    private fun createIsEmptyMethod(builder: TypeSpec.Builder, supportedFields: List<String>, defaultSharedPreferences: Boolean) {
        val methodSpecBuilder = MethodSpec.methodBuilder("isEmpty")
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement("SharedPreferences sharedPreferences = getSharedPreferences()")

        for (i in supportedFields.indices) {
            val supportedField = supportedFields[i]
            val code =
                (if (i == 0) "return" else "") + " !sharedPreferences.contains(\$S) " + (if (i == supportedFields.size - 1) ";" else "&&")
            methodSpecBuilder.addCode(code, supportedField)
        }

        builder.addMethod(methodSpecBuilder.build())
    }

    private fun createEntityContainsMethod(builder: TypeSpec.Builder, annotationKey: String, preferenceRealKey: String) {
        builder.addMethod(
            MethodSpec.methodBuilder(Common.PREFIX_CONTAINS + camelCase(annotationKey))
                .returns(TypeName.BOOLEAN)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return getSharedPreferences().contains(\$S)", preferenceRealKey)
                .build()
        )
    }

    private fun createEntitySaveMethod(builder: TypeSpec.Builder, typeElement: TypeElement, name: String, entity: ClassName) {
        // TODO: 07.09.2018 Реализовать сохранение Entity
    }
}
