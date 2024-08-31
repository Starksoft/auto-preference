package ru.starksoft.autopreferences.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import ru.starksoft.autopreferences.SharedPreference
import ru.starksoft.autopreferences.compiler.Common.createContextConstructor
import ru.starksoft.autopreferences.compiler.Common.generateFile
import ru.starksoft.autopreferences.compiler.Utils.getPreferenceFullName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("ru.starksoft.autopreferences.SharedPreference")
internal class PreferencesProcessor : AbstractProcessor() {

    lateinit var processingEnvironment: ProcessingEnvironment
        private set

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        this.processingEnvironment = processingEnv
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        val elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(
            SharedPreference::class.java
        )

        if (elementsAnnotatedWith.isNullOrEmpty()) {
            return false
        }

        val generatedPreferences: MutableList<String> = ArrayList()

        for (element in elementsAnnotatedWith) {
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.")
                return true
            }

            val typeElement = element as TypeElement

            generatedPreferences.add(getPreferenceFullName(typeElement))

            val entityCreator = EntityCreator(this)
            entityCreator.createEntityFile(typeElement)
        }

        createAppPreferences(generatedPreferences)

        return false
    }

    private fun createAppPreferences(generatedPreferences: List<String>) {
        val builder = TypeSpec.classBuilder(Common.CLASS_NAME_APP_PREFERENCES)
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        createContextConstructor(builder, true)

        builder.addField(
            FieldSpec.builder(
                ParameterizedTypeName.get(HashMap::class.java, String::class.java, Any::class.java),
                "preferencesMap",
                Modifier.PRIVATE,
                Modifier.FINAL
            ).initializer("new HashMap<>()").build()
        )

        for (generatedPreference in generatedPreferences) {
            val className = ClassName.get(Common.PACKAGE_NAME, generatedPreference)

            // GeneratedPreference lazy-init method
            builder.addMethod(
                MethodSpec.methodBuilder(Common.PREFIX_GET + generatedPreference)
                    .returns(className)
                    .addAnnotation(Common.NON_NULL_CLASS_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("\$T result = null", className)
                            .addStatement("Object item = preferencesMap.get(\"\$T\")", className)
                            .add("if (item == null) {\n")
                            .addStatement(
                                "\tpreferencesMap.put(\"\$T\", result = new \$T(context))",
                                className,
                                className
                            )
                            .add("} else {\n")
                            .addStatement("\tresult = (\$T) item", className)
                            .add("}\n")
                            .addStatement("return result")
                            .build()
                    )
                    .build()
            )
        }

        try {
            generateFile(processingEnvironment, builder.build())
        } catch (e: Throwable) {
            e.printStackTrace()
            println(e.stackTraceToString())
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    companion object {
        const val TAG: String = "PreferencesProcessor"
    }
}
