package ru.starksoft.autopreferences.compiler;

import ru.starksoft.autopreferences.PreferenceKey;
import ru.starksoft.autopreferences.SharedPreference;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static ru.starksoft.autopreferences.compiler.Common.NON_NULL_CLASS_NAME;
import static ru.starksoft.autopreferences.compiler.Common.PREFIX_GET;
import static ru.starksoft.autopreferences.compiler.Common.PREFIX_IS;

final class EntityCreator {

	private final PreferencesProcessor preferencesProcessor;

	EntityCreator(PreferencesProcessor preferencesProcessor) {
		this.preferencesProcessor = preferencesProcessor;
	}

	private static void createEntitySaveMethod(TypeSpec.Builder builder, TypeElement typeElement, String name, ClassName entity) {
		// TODO: 07.09.2018 Реализовать сохранение Entity
	}

	public void createEntityFile(TypeElement typeElement) {
		SharedPreference typeElementAnnotation = typeElement.getAnnotation(SharedPreference.class);
		String fileName = typeElementAnnotation.name();
		boolean defaultSharedPreferences = typeElementAnnotation.defaultSharedPreferences();
		boolean useCommit = typeElementAnnotation.useCommit();

		String preferenceFullName = Utils.getPreferenceFullName(typeElement);
		TypeSpec.Builder builder = TypeSpec.classBuilder(preferenceFullName).addModifiers(Modifier.FINAL, Modifier.PUBLIC);

		// Entity
		String name = typeElement.getSimpleName().toString();
		ClassName entity = ClassName.get(typeElement);

		builder.addField(Common.SHARED_PREFERENCES_CLASS_NAME, "sharedPreferences", Modifier.PRIVATE);
		builder.addField(FieldSpec.builder(String.class, "TAG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
				                 .initializer("\"" + preferenceFullName + "\"")
				                 .build());

		// Constructor
		Common.createContextConstructor(builder, false);

		// Fields
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();

		if (Utils.isNullOrEmpty(enclosedElements)) {
			throw new IllegalStateException("We need at least one field");
		}

		// getSharedPreferences() lazy-init method
		createGetSharedPreferencesMethod(fileName, defaultSharedPreferences, builder);

		// Entity creation
		createEntityMethod(builder, typeElement, name, entity);

		// Entity saving
		createEntitySaveMethod(builder, typeElement, name, entity);

		List<String> supportedFields = new ArrayList<>();

		for (Element enclosedElement : enclosedElements) {
			if (enclosedElement.getKind() == ElementKind.FIELD) {
				PreferenceKey preferenceKeyAnnotation = enclosedElement.getAnnotation(PreferenceKey.class);
				if (preferenceKeyAnnotation == null) {
					Common.printFatalMessage(preferencesProcessor.getProcessingEnvironment(),
					                         "Annotation @PreferenceKey not found on field" + enclosedElement + ", file: " + name);
					continue;
				}

				Set<Modifier> modifiers = enclosedElement.getModifiers();

				if (!modifiers.contains(Modifier.FINAL)) {
					Common.printFatalMessage(preferencesProcessor.getProcessingEnvironment(),
					                         "Only final fields supported. Current field: " + enclosedElement + ", file: " + name);
					continue;
				}

				TypeName classType = ClassName.get(enclosedElement.asType());
				String fieldClassName = classType.toString();
				SupportedTypes supportedType = SupportedTypes.findByType(fieldClassName);

				if (supportedType == null) {
					Common.printFatalMessage(preferencesProcessor.getProcessingEnvironment(), "Unsupported type: " + fieldClassName);
					continue;
				}

				String annotationKey = preferenceKeyAnnotation.value();
				boolean generateDefaultOverloadMethod = preferenceKeyAnnotation.generateDefaultOverloadMethod();
				String preferenceRealKey = getPreferenceRealKey(fileName, annotationKey);

				supportedFields.add(preferenceRealKey);

				createEntityPutMethod(builder, classType, annotationKey, preferenceRealKey, supportedType, useCommit);

				createEntityGetMethod(builder, classType, annotationKey, preferenceRealKey, supportedType, generateDefaultOverloadMethod);

				createEntityRemoveMethod(builder, annotationKey, preferenceRealKey, useCommit);

				createEntityContainsMethod(builder, annotationKey, preferenceRealKey);
			}
		}

		// Clear preference
		createRemoveAllMethod(builder, supportedFields, defaultSharedPreferences, useCommit);

		// isEmpty
		createIsEmptyMethod(builder, supportedFields, defaultSharedPreferences);

		try {
			Common.generateFile(preferencesProcessor.getProcessingEnvironment(), builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createEntityRemoveMethod(TypeSpec.Builder builder, String annotationKey, String preferenceRealKey, boolean useCommit) {
		builder.addMethod(MethodSpec.methodBuilder(Common.PREFIX_REMOVE + Utils.camelCase(annotationKey))
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("getSharedPreferences().edit().remove($S).$L()",
				                                preferenceRealKey,
				                                (useCommit ? "commit" : "apply"))
				                  .build());
	}

	private void createEntityGetMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey, String preferenceRealKey,
	                                   SupportedTypes supportedType, boolean generateDefaultOverloadMethod) {
		String name = (supportedType.equals(SupportedTypes.BOOLEAN) ? PREFIX_IS : PREFIX_GET) + Utils.camelCase(annotationKey);
		MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(name).returns(classType).addModifiers(Modifier.PUBLIC).
				addStatement("return getSharedPreferences().$L($S, $L)",
				             supportedType.getMethodName(true),
				             preferenceRealKey,
				             supportedType.getDefaultValue());

		if (supportedType == SupportedTypes.STRING) {
			methodSpecBuilder.addAnnotation(NON_NULL_CLASS_NAME);
		}

		builder.addMethod(methodSpecBuilder.build());

		// Method with default value as argument
		if (generateDefaultOverloadMethod) {
			MethodSpec.Builder defaultMethodSpecBuilder = MethodSpec.methodBuilder(name)
					.returns(classType)
					.addModifiers(Modifier.PUBLIC)
					.addParameter(ParameterSpec.builder(classType, "defaultValue").build())
					.addStatement("return getSharedPreferences().$L($S, defaultValue)",
					              supportedType.getMethodName(true),
					              preferenceRealKey);

			if (supportedType == SupportedTypes.STRING) {
				methodSpecBuilder.addAnnotation(NON_NULL_CLASS_NAME);
			}

			builder.addMethod(defaultMethodSpecBuilder.build());
		}
	}

	private void createEntityPutMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey, String preferenceRealKey,
	                                   SupportedTypes supportedType, boolean useCommit) {
		builder.addMethod(MethodSpec.methodBuilder(Common.PREFIX_PUT + Utils.camelCase(annotationKey))
				                  .returns(TypeName.VOID)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addParameter(ParameterSpec.builder(classType, "value").build())
				                  .addStatement("getSharedPreferences().edit()." + supportedType.getMethodName(false) + "($S, value).$L()",
				                                preferenceRealKey,
				                                (useCommit ? "commit" : "apply"))
				                  .build());
	}

	private String getPreferenceRealKey(String entityName, String key) {
		return entityName + "_" + key;
	}

	private void createGetSharedPreferencesMethod(String fileName, boolean defaultSharedPreferences, TypeSpec.Builder builder) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("if (sharedPreferences == null) {\n");

		if (defaultSharedPreferences) {
			codeBlockBuilder.addStatement("\tsharedPreferences = $T.getDefaultSharedPreferences(context)",
			                              Common.PREFERENCE_MANAGER_CLASS_NAME);
		} else {
			codeBlockBuilder.addStatement("\tsharedPreferences = context.getSharedPreferences($S, Context.MODE_PRIVATE)", fileName);
		}

		codeBlockBuilder.add("}\n").addStatement("return sharedPreferences");

		builder.addMethod(MethodSpec.methodBuilder("getSharedPreferences")
				                  .addAnnotation(NON_NULL_CLASS_NAME)
				                  .returns(Common.SHARED_PREFERENCES_CLASS_NAME)
				                  .addModifiers(Modifier.PRIVATE)
				                  .addCode(codeBlockBuilder.build())
				                  .build());
	}

	private void createEntityMethod(TypeSpec.Builder builder, TypeElement typeElement, String name, ClassName entity) {

		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();

		Element entityConstructor = null;
		for (Element enclosedElement : enclosedElements) {
			if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
				List<? extends VariableElement> parameters = ((ExecutableElement) enclosedElement).getParameters();

				if (!parameters.isEmpty()) {
					entityConstructor = enclosedElement;
					break;
				}
			}
		}

		if (entityConstructor == null) {
			Common.printFatalMessage(preferencesProcessor.getProcessingEnvironment(), "Constructor not found");
			return;
		}

		StringBuilder paramsBuilder = new StringBuilder();

		List<? extends VariableElement> parameters = ((ExecutableElement) entityConstructor).getParameters();

		for (int i = 0; i < parameters.size(); i++) {
			VariableElement parameter = parameters.get(i);

			Name parameterSimpleName = parameter.getSimpleName();

			for (Element enclosedElement : enclosedElements) {
				if (enclosedElement.getKind() == ElementKind.FIELD) {

					if (enclosedElement.getSimpleName().equals(parameterSimpleName)) {
						PreferenceKey annotation = enclosedElement.getAnnotation(PreferenceKey.class);

						boolean isBoolean = enclosedElement.asType().toString().equalsIgnoreCase("boolean");

						paramsBuilder.append(isBoolean ? PREFIX_IS : PREFIX_GET)
								.append(Utils.camelCase(annotation.value()))
								.append("()")
								.append(i == parameters.size() - 1 ? "" : ", ");
					}
				}
			}
		}
		String params = paramsBuilder.toString();

		if (params.isEmpty()) {
			Common.printFatalMessage(preferencesProcessor.getProcessingEnvironment(),
			                         "Cant find any parameters, PreferencesProcessor will work properly if constructor parameters and field name are the same");
			return;
		}

		builder.addMethod(MethodSpec.methodBuilder(PREFIX_GET + name)
				                  .returns(entity)
				                  .addAnnotation(NON_NULL_CLASS_NAME)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("return new $T($L)", entity, params)
				                  .build());
	}

	private void createRemoveAllMethod(TypeSpec.Builder builder, List<String> supportedFields, boolean defaultSharedPreferences,
	                                   boolean useCommit) {
		if (defaultSharedPreferences) {
			MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(Common.PREFIX_REMOVE + "All")
					.addModifiers(Modifier.PUBLIC)
					.addStatement("SharedPreferences.Editor editor = getSharedPreferences().edit()");

			for (String supportedField : supportedFields) {
				methodSpecBuilder.addStatement("editor.remove($S)", supportedField);
			}

			methodSpecBuilder.addStatement("editor.$L()", (useCommit ? "commit" : "apply"));
			builder.addMethod(methodSpecBuilder.build());

		} else {
			builder.addMethod(MethodSpec.methodBuilder(Common.PREFIX_REMOVE + "All")
					                  .addModifiers(Modifier.PUBLIC)
					                  .addStatement("getSharedPreferences().edit().clear().$L()", (useCommit ? "commit" : "apply"))
					                  .build());
		}
	}

	private void createIsEmptyMethod(TypeSpec.Builder builder, List<String> supportedFields, boolean defaultSharedPreferences) {

		MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("isEmpty")
				.addModifiers(Modifier.PUBLIC)
				.returns(TypeName.BOOLEAN)
				.addStatement("SharedPreferences sharedPreferences = getSharedPreferences()");

		for (int i = 0; i < supportedFields.size(); i++) {
			String supportedField = supportedFields.get(i);
			String code = (i == 0 ? "return" : "") + " !sharedPreferences.contains($S) " + (i == supportedFields.size() - 1 ? ";" : "&&");
			methodSpecBuilder.addCode(code, supportedField);
		}

		builder.addMethod(methodSpecBuilder.build());
	}

	private void createEntityContainsMethod(TypeSpec.Builder builder, String annotationKey, String preferenceRealKey) {
		builder.addMethod(MethodSpec.methodBuilder(Common.PREFIX_CONTAINS + Utils.camelCase(annotationKey))
				                  .returns(TypeName.BOOLEAN)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("return getSharedPreferences().contains($S)", preferenceRealKey)
				                  .build());
	}
}