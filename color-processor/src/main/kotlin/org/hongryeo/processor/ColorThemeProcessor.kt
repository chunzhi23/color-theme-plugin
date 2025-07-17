package org.hongryeo.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class ColorThemeProcessor(
    private val codeGen: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("org.hongryeo.annotations.ColorTheme")
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDecl ->
                generateThemeImpl(classDecl)
            }
        return emptyList()
    }

    private fun generateThemeImpl(classDecl: KSClassDeclaration) {
        val pkg = classDecl.packageName.asString()
        val name = classDecl.simpleName.asString()
        val genName = "${name}Generated"

        val props = classDecl.getDeclaredProperties()
            .filter { it.annotations.any { a -> a.shortName.asString() == "ColorField" } }
            .map {
                val propName = it.simpleName.asString()
                val args = it.annotations.first { a -> a.shortName.asString() == "ColorField" }
                    .arguments.associate { arg -> arg.name!!.asString() to arg.value as String }
                propName to (args["light"]!! to args["dark"]!!)
            }

        val obj = TypeSpec.objectBuilder(genName)
            .addSuperinterface(ClassName(pkg, name))
            .addProperty(
                PropertySpec.builder("isDark", Boolean::class)
                    .mutable(true).initializer("false").build()
            )
            .addInitializerBlock(CodeBlock.builder().apply {
                for ((p, pair) in props) {
                    addStatement(
                        "%L = if(isDark) Color(${pair.second}) else Color(${pair.first})",
                        p
                    )
                }
            }.build())
            .build()

        val file = FileSpec.builder(pkg, genName)
            .addImport("androidx.compose.ui.graphics", "Color")
            .addType(obj).build()

        file.writeTo(codeGen, Dependencies(false))
    }
}
