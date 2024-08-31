package ru.starksoft.autopreferences.compiler;

import ru.starksoft.autopreferences.SharedPreference;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"ru.starksoft.autopreferences.SharedPreference"})
public class PreferencesProcessor extends AbstractProcessor {

	static final String TAG = "PreferencesProcessor";
	private ProcessingEnvironment processingEnvironment;

	public ProcessingEnvironment getProcessingEnvironment() {
		return processingEnvironment;
	}

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

			generatedPreferences.add(Utils.getPreferenceFullName(typeElement));

			EntityCreator entityCreator = new EntityCreator(this);
			entityCreator.createEntityFile(typeElement);
		}

		createAppPreferences(generatedPreferences);

		return false;
	}

	private void createAppPreferences(List<String> generatedPreferences) {
		TypeSpec.Builder builder = TypeSpec.classBuilder(Common.CLASS_NAME_APP_PREFERENCES);
		builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		Common.createContextConstructor(builder, true);

		builder.addField(FieldSpec.builder(ParameterizedTypeName.get(HashMap.class, String.class, Object.class),
		                                   "preferencesMap",
		                                   Modifier.PRIVATE,
		                                   Modifier.FINAL).initializer("new HashMap<>()").build());

		for (String generatedPreference : generatedPreferences) {

			ClassName className = ClassName.get(Common.PACKAGE_NAME, generatedPreference);

			// GeneratedPreference lazy-init method
			builder.addMethod(MethodSpec.methodBuilder(Common.PREFIX_GET + generatedPreference)
					                  .returns(className)
					                  .addAnnotation(Common.NON_NULL_CLASS_NAME)
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
			Common.generateFile(processingEnvironment, builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}
}
