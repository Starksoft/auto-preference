package ru.starksoft.autopreferences.compiler

import com.squareup.kotlinpoet.*
import ru.starksoft.autopreferences.PreferenceKey
import ru.starksoft.autopreferences.SharedPreference
import java.io.IOException
import javax.lang.model.element.*

internal class EntityCreator(private val preferencesProcessor: PreferencesProcessor) {

	private fun createEntitySaveMethod(builder: TypeSpec.Builder, typeElement: TypeElement, name: String, entity: ClassName) {
		// TODO: 07.09.2018 Реализовать сохранение Entity
	}

	fun createEntityFile(typeElement: TypeElement) {
		val typeElementAnnotation = typeElement.getAnnotation(SharedPreference::class.java)
		val fileName = typeElementAnnotation.name
		val defaultSharedPreferences = typeElementAnnotation.defaultSharedPreferences
		val useCommit = typeElementAnnotation.useCommit

		val preferenceFullName = typeElement.getPreferenceFullName()
		val builder = FileSpec.builder(PACKAGE_NAME, preferenceFullName)

		// Entity
		val name = typeElement.simpleName.toString()
		val entity = ClassName.bestGuess(typeElement.toString())

		builder.addProperty(PropertySpec.builder("TAG", String::class, KModifier.PRIVATE, KModifier.CONST)
									.initializer("\"" + preferenceFullName + "\"")
									.build())

		// Create Class
		val classBuilder = TypeSpec.classBuilder(preferenceFullName)
		classBuilder.addModifiers(KModifier.FINAL)

		// Create Constructor
		classBuilder.createContextConstructor(preferenceFullName, false)

		// Fields
		val enclosedElements = typeElement.enclosedElements

		if (enclosedElements.isNullOrEmpty()) {
			throw IllegalStateException("We need at least one field")
		}

		// getSharedPreferences() lazy-init method
		createGetSharedPreferencesMethod(fileName, defaultSharedPreferences, classBuilder)

		// Entity get
		createEntityMethod(classBuilder, typeElement, name, entity)
		// Entity save
		createEntitySaveMethod(classBuilder, typeElement, name, entity)

		val supportedFields = ArrayList<String>()

		for (enclosedElement in enclosedElements) {
			if (enclosedElement.kind == ElementKind.FIELD) {
				val preferenceKeyAnnotation = enclosedElement.getAnnotation(PreferenceKey::class.java)
				if (preferenceKeyAnnotation == null) {
					preferencesProcessor.getProcessingEnvironment()?.printFatalMessage(
							"Annotation @PreferenceKey not found on field$enclosedElement, file: $name")
					continue
				}

				val modifiers = enclosedElement.modifiers

				if (!modifiers.contains(Modifier.FINAL)) {
					preferencesProcessor.getProcessingEnvironment()?.printFatalMessage(
							"Only final fields supported. Current field: $enclosedElement, file: $name")
					continue
				}

				val classNameString = enclosedElement.asType().toString()
				val supportedType = SupportedTypes.findByType(classNameString)

				if (supportedType == null) {
					preferencesProcessor.getProcessingEnvironment()?.printFatalMessage("Unsupported type: $classNameString")
					continue
				}

				val classType = ClassName.bestGuess(supportedType.kotlinClass.toString())

				val annotationKey = preferenceKeyAnnotation.value
				val generateDefaultOverloadMethod = preferenceKeyAnnotation.generateDefaultOverloadMethod
				val preferenceRealKey = getPreferenceRealKey(fileName, annotationKey)

				supportedFields.add(preferenceRealKey)

				createEntityPutMethod(classBuilder, annotationKey, preferenceRealKey, supportedType, useCommit)

				createEntityGetMethod(classBuilder,
									  classType,
									  annotationKey,
									  preferenceRealKey,
									  supportedType,
									  generateDefaultOverloadMethod)

				createEntityRemoveMethod(classBuilder, annotationKey, preferenceRealKey, useCommit)

				createEntityContainsMethod(classBuilder, annotationKey, preferenceRealKey)
			}
		}

		// Clear preference
		createRemoveAllMethod(classBuilder, supportedFields, defaultSharedPreferences, useCommit)

		// isEmpty
		createIsEmptyMethod(classBuilder, supportedFields, defaultSharedPreferences)

		// Save class to kotlin file
		builder.addType(classBuilder.build())

		try {
			preferencesProcessor.getProcessingEnvironment()?.generateFile(builder.build())
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun createEntityRemoveMethod(builder: TypeSpec.Builder, annotationKey: String, preferenceRealKey: String, useCommit: Boolean) {

		builder.addFunction(
				FunSpec.builder(PREFIX_REMOVE + annotationKey.camelCase())
						.addStatement("getSharedPreferences().edit().remove(%S).%L()",
									  preferenceRealKey,
									  if (useCommit) "commit" else "apply")
						.build())
	}

	private fun createEntityGetMethod(builder: TypeSpec.Builder, classType: TypeName?, annotationKey: String, preferenceRealKey: String,
									  supportedType: SupportedTypes, generateDefaultOverloadMethod: Boolean
	) {
		val name = (if (supportedType == SupportedTypes.BOOLEAN) PREFIX_IS else PREFIX_GET) + annotationKey.camelCase()

		builder.addFunction(FunSpec
									.builder(name)
									.addStatement("return getSharedPreferences().%L(%S, %L)", supportedType.getMethodName(true),
												  preferenceRealKey,
												  supportedType.defaultValue).build())

		// Method with default value as argument
		if (generateDefaultOverloadMethod) {
			builder.addFunction(FunSpec.builder(name)
										.addParameter(ParameterSpec
															  .builder("defaultValue", supportedType.kotlinClass)
															  .build())
										.addStatement("return getSharedPreferences().%L(%S, defaultValue)",
													  supportedType.getMethodName(true),
													  preferenceRealKey)
										.build())
		}
	}

	private fun createEntityPutMethod(builder: TypeSpec.Builder,
									  annotationKey: String,
									  preferenceRealKey: String,
									  supportedType: SupportedTypes,
									  useCommit: Boolean
	) {
		builder.addFunction(FunSpec.builder(PREFIX_PUT + annotationKey.camelCase())
									.addParameter(ParameterSpec.builder("value", supportedType.kotlinClass).build())
									.addStatement("getSharedPreferences().edit()." + supportedType.getMethodName(false) + "(%S, value).%L()",
												  preferenceRealKey,
												  if (useCommit) "commit" else "apply")
									.build())
	}

	private fun getPreferenceRealKey(entityName: String, key: String): String {
		return entityName + "_" + key
	}

	private fun createGetSharedPreferencesMethod(fileName: String, defaultSharedPreferences: Boolean, builder: TypeSpec.Builder) {
		val codeBlockBuilder = CodeBlock.builder().add("if (!::sharedPreferences.isInitialized) {\n")

		if (defaultSharedPreferences) {
			codeBlockBuilder.addStatement("\tsharedPreferences = %T.getDefaultSharedPreferences(context)",
										  PREFERENCE_MANAGER_CLASS_NAME)
		} else {
			codeBlockBuilder.addStatement("\tsharedPreferences = context.getSharedPreferences(%S, Context.MODE_PRIVATE)", fileName)
		}

		codeBlockBuilder.add("}\n").addStatement("return sharedPreferences")

		builder.addProperty(PropertySpec.builder("sharedPreferences",
												 SHARED_PREFERENCES_CLASS_NAME,
												 KModifier.PRIVATE,
												 KModifier.LATEINIT).mutable().build())

		builder.addFunction(FunSpec.builder("getSharedPreferences")
									.returns(SHARED_PREFERENCES_CLASS_NAME)
									.addCode(codeBlockBuilder.build())
									.build())
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
			preferencesProcessor.getProcessingEnvironment()?.printFatalMessage("Constructor not found")
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
								.append(annotation.value.camelCase())
								.append("()")
								.append(if (i == parameters.size - 1) "" else ", ")
					}
				}
			}
		}
		val params = paramsBuilder.toString()

		if (params.isEmpty()) {
			preferencesProcessor
					.getProcessingEnvironment()
					?.printFatalMessage(
							"Cant find any parameters, PreferencesProcessor will work properly if constructor parameters and field name are the same")
			return
		}

		builder.addFunction(FunSpec.builder(PREFIX_GET + name)
									.addStatement("return %T(%L)", entity, params)
									.returns(entity)
									.build())
	}

	private fun createRemoveAllMethod(builder: TypeSpec.Builder, supportedFields: List<String>, defaultSharedPreferences: Boolean,
									  useCommit: Boolean
	) {
		if (defaultSharedPreferences) {
			val methodSpecBuilder = FunSpec.builder(PREFIX_REMOVE + "All")
					.addStatement("val editor = getSharedPreferences().edit()")

			for (supportedField in supportedFields) {
				methodSpecBuilder.addStatement("editor.remove(%S)", supportedField)
			}

			methodSpecBuilder.addStatement("editor.%L()", if (useCommit) "commit" else "apply")
			builder.addFunction(methodSpecBuilder.build())

		} else {
			builder.addFunction(FunSpec.builder(PREFIX_REMOVE + "All")
										.addStatement("getSharedPreferences().edit().clear().%L()", if (useCommit) "commit" else "apply")
										.build())
		}
	}

	private fun createIsEmptyMethod(builder: TypeSpec.Builder, supportedFields: List<String>, defaultSharedPreferences: Boolean) {

		val methodSpecBuilder = FunSpec.builder("isEmpty")
				.returns(BOOLEAN)
				.addStatement("val sharedPreferences = getSharedPreferences()")

		for (i in supportedFields.indices) {
			val supportedField = supportedFields[i]
			val code = (if (i == 0) "return" else "") + " !sharedPreferences.contains(%S) " + if (i == supportedFields.size - 1) "\n" else "&&"
			methodSpecBuilder.addCode(code, supportedField)
		}

		builder.addFunction(methodSpecBuilder.build())
	}

	private fun createEntityContainsMethod(builder: TypeSpec.Builder, annotationKey: String, preferenceRealKey: String) {
		builder.addFunction(FunSpec.builder(PREFIX_CONTAINS + annotationKey.camelCase())
									.addStatement("return getSharedPreferences().contains(%S)",
												  preferenceRealKey).build())
	}
}