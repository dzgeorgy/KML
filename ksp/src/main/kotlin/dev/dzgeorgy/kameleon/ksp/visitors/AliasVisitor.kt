package dev.dzgeorgy.kameleon.ksp.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import dev.dzgeorgy.kameleon.lib.Alias

class AliasVisitor(
    private val logger: KSPLogger
) : KSDefaultVisitor<Unit, Pair<KSNode, Set<String>>>() {

    override fun defaultHandler(node: KSNode, data: Unit): Pair<KSNode, Set<String>> {
        return node to emptySet()
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit): Pair<KSNode, Set<String>> {
        return valueParameter to buildSet {
            add(valueParameter.name!!.asString())
            addAll(valueParameter.getAliases())
        }
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): Pair<KSNode, Set<String>> {
        return property to buildSet {
            add(property.simpleName.asString())
            addAll(property.getAliases())
        }
    }

    private fun KSAnnotated.getAliases(): List<String> {
        val aliasAnnotation = annotations.first { it.shortName.asString() == Alias::class.simpleName.toString() }

        @Suppress("UNCHECKED_CAST")
        val aliases: ArrayList<String> = aliasAnnotation.arguments.first().value as ArrayList<String>
        return aliases.toList()
    }

}
