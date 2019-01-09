package ru.starksoft.autopreferences.compiler

//import com.google.auto.service.AutoService
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.starksoft.autopreferences.SharedPreference
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

const val TAG = "PreferencesProcessor"

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("ru.starksoft.autopreferences.SharedPreference")
class PreferencesProcessor : AbstractProcessor() {

	private lateinit var processingEnvironment: ProcessingEnvironment

	fun getProcessingEnvironment(): ProcessingEnvironment? {
		return processingEnvironment
	}

	@Synchronized
	override fun init(processingEnv: ProcessingEnvironment) {
		this.processingEnvironment = processingEnv
	}

	override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

		val elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(SharedPreference::class.java)

		if (elementsAnnotatedWith == null || elementsAnnotatedWith.isEmpty()) {
			return false
		}

		val generatedPreferences = ArrayList<String>()

		for (element in elementsAnnotatedWith) {
			if (element.kind != ElementKind.CLASS) {
				processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.")
				return true
			}

			val typeElement = element as TypeElement

			generatedPreferences.add(typeElement.getPreferenceFullName())

			EntityCreator(this).createEntityFile(typeElement)
		}

		createAppPreferences(generatedPreferences)

		return false
	}

	private fun createAppPreferences(generatedPreferences: List<String>) {
		val fileSpec = FileSpec.builder(PACKAGE_NAME, CLASS_NAME_APP_PREFERENCES)

		// Create Class
		val classBuilder = TypeSpec.classBuilder(CLASS_NAME_APP_PREFERENCES)

		classBuilder.addModifiers(KModifier.FINAL)

		classBuilder.createContextConstructor(CLASS_NAME_APP_PREFERENCES, true)


		val preferencesMap = ClassName(PACKAGE_NAME, "HashMap")

		classBuilder
				.addProperty(PropertySpec
									 .builder("preferencesMap",
											  preferencesMap.parameterizedBy(String::class.asTypeName(), Any::class.asTypeName()),
											  KModifier.PRIVATE)
									 .initializer("HashMap()")
									 .build())

		for (generatedPreference in generatedPreferences) {

			val className = ClassName(PACKAGE_NAME, generatedPreference)

			// GeneratedPreference lazy-init method
			classBuilder.addFunction(FunSpec.builder(PREFIX_GET + generatedPreference)
											 .returns(className)
											 .addCode(CodeBlock.builder()
															  .addStatement("val result = preferencesMap.getOrPut(\"%T\", {", className)
															  .addStatement("\t%T(context)", className)
															  .addStatement("})", className)
															  .addStatement("return result as %T", className)
															  .build())

											 .build())
		}

		fileSpec.addType(classBuilder.build())

		try {
			processingEnvironment.generateFile(fileSpec.build())
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
}