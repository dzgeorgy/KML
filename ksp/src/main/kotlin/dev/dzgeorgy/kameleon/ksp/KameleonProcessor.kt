package dev.dzgeorgy.kameleon.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.dzgeorgy.kameleon.ksp.visitors.AliasVisitor
import dev.dzgeorgy.kameleon.ksp.visitors.MapToVisitor
import dev.dzgeorgy.kameleon.lib.Alias
import dev.dzgeorgy.kameleon.lib.MapTo

typealias AliasesCache = Map<KSNode, Set<String>>

class KameleonProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    // Status
    private var firstRound = true

    // Aliases
    private val aliasVisitor = AliasVisitor(logger)
    private val _aliasesCache: HashMap<KSNode, Set<String>> = hashMapOf()
    private val aliasesCache: AliasesCache
        get() = _aliasesCache

    // Mappers
    private val mapToVisitor = MapToVisitor(logger, aliasesCache)
    override fun process(resolver: Resolver): List<KSAnnotated> {

        // Processing annotations only once.
        if (firstRound) {
            val aliasSequence = resolver.getSymbolsWithAnnotation(Alias::class.qualifiedName!!)

            aliasSequence.map { it.accept(aliasVisitor, Unit) }.forEach {
                _aliasesCache[it.first] = it.second
            }

            firstRound = false
        }

        val mappingSequence = resolver.getSymbolsWithAnnotation(MapTo::class.qualifiedName!!)

        val mappers = mappingSequence.map { it.accept(mapToVisitor, Unit) }
            .forEach { data ->
                data.groupBy { it.from }
                    .forEach { (source, targets) ->
                        val mappers = targets.map {
                            FunSpec.builder("to${it.to}")
                                .receiver(it.from.toClassName())
                                .returns(it.to.toClassName())
                                .addKdoc("${it.from} -> ${it.to}")
                                .addCode(
                                    """return ${
                                        it.mapping.map { (k, v) -> "$k = this.$v" }.joinToString(
                                            separator = ", ",
                                            prefix = "${it.to}(",
                                            postfix = ")"
                                        )
                                    }
                                    """.trimIndent()
                                )
                                .build()
                        }
                        val file = FileSpec.builder(source.packageName.asString(), "${source}Mapper")
                            .addFunctions(mappers)
                            .build()
                            .writeTo(codeGenerator, false)
                    }
            }

        return emptyList()
    }

}
