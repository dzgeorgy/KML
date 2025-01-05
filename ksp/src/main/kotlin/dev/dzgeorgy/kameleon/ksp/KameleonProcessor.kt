package dev.dzgeorgy.kameleon.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.dzgeorgy.kameleon.ksp.visitors.MapToVisitor
import dev.dzgeorgy.kameleon.lib.MapTo

class KameleonProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapToSymbols = resolver.getSymbolsWithAnnotation(MapTo::class.qualifiedName!!)

        val mapToVisitor = MapToVisitor(logger)

        mapToSymbols.forEach {
            it.accept(mapToVisitor, Unit)
        }

        mapToVisitor.getResults()
            .groupBy { it.source }
            .forEach { (source, data) ->

                // Create file.
                val fileSpec = FileSpec.builder(
                    packageName = source.qualifiedName?.getQualifier().orEmpty(),
                    fileName = "${source}Mapper"
                )

                // Process mapping data.
                data.forEach {

                    // Create mapper.
                    val mapper = FunSpec.builder("to${it.target}")
                        .receiver(it.source.toClassName())
                        .returns(it.target.toClassName())
                        .addKdoc("${it.source} -> ${it.target}")
                        .addCode(
                            """
                        return ${
                                it.mappedData.map { (k, v) -> "$k = this.$v" }
                                    .joinToString(
                                        separator = ", ",
                                        prefix = "${it.target}(",
                                        postfix = ")"
                                    )
                            }
                    """.trimIndent())
                        .build()

                    // Add mapper to file.
                    fileSpec.addFunction(mapper)
                }

                // Write file.
                fileSpec.build().writeTo(codeGenerator, false)
            }

        return emptyList()
    }

}
