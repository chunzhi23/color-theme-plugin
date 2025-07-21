package io.github.chunzhi23.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class ColorThemeProcessor(
    private val codeGen: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    companion object {
        private const val ANNOTATION_THEME = "io.github.chunzhi23.annotations.ColorTheme"
        private const val ANNOTATION_FIELD = "io.github.chunzhi23.annotations.ColorField"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_THEME)
            .filterIsInstance<KSClassDeclaration>()
        symbols.forEach { classDecl ->
            logger.warn(">> Generating colors for ${classDecl.simpleName.asString()}")
            generateColors(classDecl)
        }
        return emptyList()
    }

    private fun generateColors(classDecl: KSClassDeclaration) {
        val pkg = classDecl.packageName.asString()
        val genName = classDecl.simpleName.asString()

        val fields = classDecl.getDeclaredProperties()
            .filter { prop ->
                prop.annotations.any {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_FIELD
                }
            }
            .map { prop ->
                val fieldName = prop.simpleName.asString()
                val (light, dark) = prop.annotations
                    .first { ann ->
                        ann.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_FIELD
                    }
                    .arguments
                    .associate { arg ->
                        arg.name!!.asString() to (arg.value as? String
                            ?: throw IllegalArgumentException(
                                "@ColorField.${arg.name} must be a String literal"
                            ))
                    }
                    .let { map ->
                        val l = map["light"]
                            ?: throw IllegalArgumentException("@ColorField.light missing")
                        val d = map["dark"]
                            ?: throw IllegalArgumentException("@ColorField.dark missing")
                        l to d
                    }
                fieldName to (light to dark)
            }

        val objSpec = TypeSpec.objectBuilder(genName).apply {
            fields.forEach { (fieldName, pair) ->
                addProperty(
                    PropertySpec.builder(
                        fieldName,
                        ClassName("androidx.compose.ui.graphics", "Color")
                    )
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement(
                                    "return if(%T.isDark) Color(${pair.second}) else Color(${pair.first})",
                                    ClassName(pkg, genName)
                                )
                                .build()
                        )
                        .build()
                )
            }
        }.build()

        FileSpec.builder(pkg, genName)
            .addImport("androidx.compose.ui.graphics", "Color")
            .addType(objSpec)
            .build()
            .writeTo(
                codeGen,
                Dependencies(aggregating = false, classDecl.containingFile!!)
            )
    }
}