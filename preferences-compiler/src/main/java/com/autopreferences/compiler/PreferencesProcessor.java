package com.autopreferences.compiler;

import com.autopreferences.annotations.PreferenceKey;
import com.autopreferences.annotations.SharedPreference;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.autopreferences.annotations.SharedPreference"})
public class PreferencesProcessor extends AbstractProcessor {

	private static final String TAG = PreferencesProcessor.class.getSimpleName();

	private static final String PREFIX_GET = "get";
	private static final String PREFIX_IS = "is";
	private static final String PREFIX_PUT = "put";
	private static final String PREFIX_REMOVE = "remove";
	private static final String PREFIX_CONTAINS = "contains";

	private static final String PACKAGE_NAME = "com.autopreferences.build";
	private static final String CLASS_NAME_APP_PREFERENCES = "AppPreferences";
	private static final ClassName SHARED_PREFERENCES_CLASS_NAME = ClassName.get("android.content", "SharedPreferences");
	private ProcessingEnvironment processingEnvironment;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		this.processingEnvironment = processingEnv;
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

		Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(SharedPreference.class);

		if (elementsAnnotatedWith == null || elementsAnnotatedWith.isEmpty()) {
			return false;
		}

		List<String> generatedPreferences = new ArrayList<>();

		for (Element element : elementsAnnotatedWith) {
			if (element.getKind() != ElementKind.CLASS) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.");
				return true;
			}

			TypeElement typeElement = (TypeElement) element;

			generatedPreferences.add(getPreferenceFullName(typeElement));

			createEntityFile(typeElement);
		}

		createAppPreferences(generatedPreferences);

		return false;
	}

	private void createEntityFile(TypeElement typeElement) {
		String fileName = typeElement.getAnnotation(SharedPreference.class).name();
		TypeSpec.Builder builder = TypeSpec.classBuilder(getPreferenceFullName(typeElement)).addModifiers(Modifier.FINAL, Modifier.PUBLIC);

		// UserEntity
		String name = typeElement.getSimpleName().toString();
		ClassName entity = ClassName.get(typeElement);

		builder.addField(SHARED_PREFERENCES_CLASS_NAME, "sharedPreferences", Modifier.PRIVATE);

		// Constructor
		createContextConstructor(builder, false);

		// Fields
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();

		if (Utils.isNullOrEmpty(enclosedElements)) {
			throw new IllegalStateException("We need at least one field");
		}

		// getSharedPreferences() lazy-init method
		builder.addMethod(MethodSpec.methodBuilder("getSharedPreferences")
				                  .returns(SHARED_PREFERENCES_CLASS_NAME)
				                  .addModifiers(Modifier.PRIVATE)
				                  .addCode(CodeBlock.builder()
						                           .add("if (sharedPreferences == null) {\n")
						                           .addStatement(
								                           "\tsharedPreferences = context.getSharedPreferences($S, Context.MODE_PRIVATE)",
								                           fileName)
						                           .add("}\n")
						                           .addStatement("return sharedPreferences")
						                           .build())
				                  .build());

		// Entity creation
		createEntityMethod(builder, typeElement, name, entity);

		// Entity saving
		createEntityPutMethod(builder, typeElement, name, entity);

		// Clear preference
		createRemoveAllMethod(builder);

		for (Element enclosedElement : enclosedElements) {
			if (enclosedElement.getKind() == ElementKind.FIELD) {
				PreferenceKey annotation = enclosedElement.getAnnotation(PreferenceKey.class);
				if (annotation == null) {
					printFatalMessage("Annotation @PreferenceKey not found on field" + enclosedElement + ", file: " + name);
					continue;
				}

				Set<Modifier> modifiers = enclosedElement.getModifiers();

				if (!modifiers.contains(Modifier.FINAL)) {
					printFatalMessage("Only final fields supported. Current field: " + enclosedElement + ", file: " + name);
					continue;
				}

				TypeName classType = ClassName.get(enclosedElement.asType());
				String fieldClassName = classType.toString();
				SupportedTypes supportedType = SupportedTypes.findByType(fieldClassName);

				if (supportedType == null) {
					printFatalMessage("Unsupported type: " + fieldClassName);
					continue;
				}

				String annotationKey = annotation.value();

				createEntityPutMethod(builder, classType, annotationKey, supportedType);

				createEntityGetMethod(builder, classType, annotationKey, supportedType);

				createEntityRemoveMethod(builder, classType, annotationKey, supportedType);

				createEntityContainsMethod(builder, classType, annotationKey, supportedType);
			}
		}

		try {
			generateFile(builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createRemoveAllMethod(TypeSpec.Builder builder) {
		builder.addMethod(MethodSpec.methodBuilder(PREFIX_REMOVE + "All")
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("getSharedPreferences().edit().clear().apply()")
				                  .build());
	}

	private void printFatalMessage(String message) {
		processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "\n\n\n" + TAG + " " + message);
	}

	private String getPreferenceFullName(TypeElement typeElement) {
		return Utils.camelCase(typeElement.getAnnotation(SharedPreference.class).name()) + "Preferences";
	}

	private void createEntityGetMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey, SupportedTypes supportedType) {

		builder.addMethod(MethodSpec.methodBuilder(
				(supportedType.equals(SupportedTypes.BOOLEAN) ? PREFIX_IS : PREFIX_GET) + Utils.camelCase(annotationKey))
				                  .returns(classType)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("return getSharedPreferences()." + supportedType.getMethodName(true) + "($S, " +
						                                supportedType.defaultValue + ")", annotationKey)
				                  .build());
	}

	private void createEntityPutMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey, SupportedTypes supportedType) {
		builder.addMethod(MethodSpec.methodBuilder(PREFIX_PUT + Utils.camelCase(annotationKey))
				                  .returns(TypeName.VOID)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addParameter(ParameterSpec.builder(classType, "value").build())
				                  .addStatement(
						                  "getSharedPreferences().edit()." + supportedType.getMethodName(false) + "($S, value).apply()",
						                  annotationKey)
				                  .build());
	}

	private void createEntityRemoveMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey,
	                                      SupportedTypes supportedType) {
		builder.addMethod(MethodSpec.methodBuilder(PREFIX_REMOVE + Utils.camelCase(annotationKey))
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("getSharedPreferences().edit().remove($S).apply()", annotationKey)
				                  .build());
	}

	private void createEntityContainsMethod(TypeSpec.Builder builder, TypeName classType, String annotationKey,
	                                        SupportedTypes supportedType) {
		builder.addMethod(MethodSpec.methodBuilder(PREFIX_CONTAINS + Utils.camelCase(annotationKey))
				                  .returns(TypeName.BOOLEAN)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("return getSharedPreferences().contains($S)", annotationKey)
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
			printFatalMessage("Constructor not found");
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
			printFatalMessage(
					"Cant find any parameters, PreferencesProcessor will work properly if constructor parameters and field name are the same");
			return;
		}

		builder.addMethod(MethodSpec.methodBuilder(PREFIX_GET + name)
				                  .returns(entity)
				                  .addModifiers(Modifier.PUBLIC)
				                  .addStatement("return new $T($L)", entity, params)
				                  .build());
	}

	private void createEntityPutMethod(TypeSpec.Builder builder, TypeElement typeElement, String name, ClassName entity) {
		// TODO: 07.09.2018 Реализовать сохранение Entity

		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();

		for (Element enclosedElement : enclosedElements) {
			if (enclosedElement.getKind() == ElementKind.FIELD) {

				List<? extends Element> enclosedElementsOfField = enclosedElement.getEnclosedElements();


			} else if (enclosedElement.getKind() == ElementKind.METHOD) {
				List<? extends Element> enclosedElementsOfField = enclosedElement.getEnclosedElements();


			}
		}


	}

	private void createAppPreferences(List<String> generatedPreferences) {
		TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME_APP_PREFERENCES);
		builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		createContextConstructor(builder, true);

		builder.addField(FieldSpec.builder(ParameterizedTypeName.get(HashMap.class, String.class, Object.class),
		                                   "preferencesMap",
		                                   Modifier.PRIVATE,
		                                   Modifier.FINAL).initializer("new HashMap<>()").build());

		for (String generatedPreference : generatedPreferences) {

			ClassName className = ClassName.get(PACKAGE_NAME, generatedPreference);

			// GeneratedPreference lazy-init method
			builder.addMethod(MethodSpec.methodBuilder(PREFIX_GET + generatedPreference)
					                  .returns(className)
					                  .addModifiers(Modifier.PUBLIC)
					                  .addCode(CodeBlock.builder()
							                           .addStatement("$T result = null", className)
							                           .addStatement("Object item = preferencesMap.get(\"$T\")", className)
							                           .add("if (item == null) {\n")
							                           .addStatement("\tpreferencesMap.put(\"$T\", result = new $T(context))",
							                                         className,
							                                         className)
							                           .add("} else {\n")
							                           .addStatement("\tresult = ($T) item", className)
							                           .add("}\n")
							                           .addStatement("return result")
							                           .build())
					                  .build());
		}

		try {
			generateFile(builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createContextConstructor(TypeSpec.Builder builder, boolean publicConstructor) {
		ClassName context = ClassName.get("android.content", "Context");
		builder.addField(context, "context", Modifier.PRIVATE, Modifier.FINAL);

		// Constructor
		MethodSpec.Builder methodSpecBuilder =
				MethodSpec.constructorBuilder().addParameter(context, "context").addStatement("this.context = context");

		if (publicConstructor) {
			methodSpecBuilder.addModifiers(Modifier.PUBLIC);
		}

		builder.addMethod(methodSpecBuilder.build());
	}

	private void generateFile(final TypeSpec typeSpec) throws IOException {
		JavaFile.builder(PACKAGE_NAME, typeSpec).build().writeTo(processingEnvironment.getFiler());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	enum SupportedTypes {

		INT("0", Arrays.asList("int", Integer.class.getName())),
		STRING("\"\"", Arrays.asList(String.class.getName())),
		BOOLEAN("false", Arrays.asList("boolean", Boolean.class.getName())),
		FLOAT("0f", Arrays.asList("float", Float.class.getName())),
		LONG("0l", Arrays.asList("long", Long.class.getName()));

		private final String defaultValue;
		private final List<String> typeNameList;

		SupportedTypes(String defaultValue, List<String> typeNameList) {
			this.defaultValue = defaultValue;
			this.typeNameList = typeNameList;
		}

		static SupportedTypes findByType(String type) {
			SupportedTypes[] values = SupportedTypes.values();
			for (SupportedTypes value : values) {

				List<String> typeNameList = value.typeNameList;
				for (String name : typeNameList) {
					if (type.equalsIgnoreCase(name)) {
						return value;
					}
				}
			}
			return null;
		}

		String getMethodName(boolean get) {
			return (get ? "get" : "put") + Utils.camelCase(name().toLowerCase());
		}
	}
}