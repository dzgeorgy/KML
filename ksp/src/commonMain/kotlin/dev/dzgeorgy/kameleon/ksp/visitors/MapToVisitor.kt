package dev.dzgeorgy.kameleon.ksp.visitors

import com.google.devtools.ksp.outerType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import dev.dzgeorgy.kameleon.ksp.AliasesCache
import dev.dzgeorgy.kameleon.lib.MapTo
import dev.dzgeorgy.kameleon.lib.MappingDirection

class MapToVisitor(
    private val logger: KSPLogger,
    private val aliases: AliasesCache
) : KSDefaultVisitor<Unit, Sequence<MappingData>>() {

    private val propertiesCache: MutableMap<KSClassDeclaration, List<PropertyData>> = hashMapOf()
    private val parametersCache: MutableMap<KSClassDeclaration, List<ParameterData>> = hashMapOf()

    override fun defaultHandler(node: KSNode, data: Unit): Sequence<MappingData> {
        logger.error("DEFAULT HANDLER CALLED FOR: $node")
        throw IllegalArgumentException("")
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Sequence<MappingData> {
        return classDeclaration.getMapToData()
            .flatMap { mapToData ->
                when (mapToData.direction) {
                    MappingDirection.FROM -> listOf(createMappingData(classDeclaration, mapToData.target))
                    MappingDirection.TO -> listOf(createMappingData(mapToData.target, classDeclaration))
                    MappingDirection.BOTH -> listOf(createMappingData(classDeclaration, mapToData.target), createMappingData(mapToData.target, classDeclaration))
                }
            }
    }

    private fun createMappingData(from: KSClassDeclaration, to: KSClassDeclaration): MappingData {
        val properties = from.getPropertiesToMap()
        val parameters = to.getConstructorParametersToMap()
        val mapping = associateParametersWithProperties(parameters, properties)
        return MappingData(from, to, mapping)
    }

    private fun associateParametersWithProperties(
        parameters: List<ParameterData>,
        properties: List<PropertyData>
    ): Map<String, String> {
        val map = parameters.associateWith { parameter ->
            properties.firstOrNull { property ->
                nameMatcher(property, parameter) && typeMatcher(property, parameter)
            } ?: properties.firstOrNull { property ->
                aliasesMatcher(property, parameter) && typeMatcher(property, parameter)
            } ?: run {
                logger.error(
                    "Unable to find matching property for ${parameter.name}: ${parameter.type}.",
                    parameter.reference
                )
                return emptyMap()
            }
        }.mapKeys { it.key.name }.mapValues { it.value.name }

        return map
    }

    private fun nameMatcher(property: PropertyData, parameter: ParameterData): Boolean {
        return property.name == parameter.name
    }

    private fun typeMatcher(property: PropertyData, parameter: ParameterData): Boolean {
        return parameter.type.isAssignableFrom(property.type)
    }

    private fun aliasesMatcher(property: PropertyData, parameter: ParameterData): Boolean {
        val propertyAliases = aliases[property.reference] ?: setOf(property.name)
        val parameterAliases = aliases[parameter.reference] ?: setOf(parameter.name)
        return (propertyAliases intersect parameterAliases).isNotEmpty()
    }

    private fun KSClassDeclaration.getMapToData(): Sequence<MapToAnnotationData> {
        return annotations.filter { it.shortName.asString() == MapTo::class.simpleName.toString() }
            .map { annotation ->
                val targetType: KSType = annotation.arguments.first().value as KSType
                val direction =
                    MappingDirection.valueOf((annotation.arguments[1].value as KSType).declaration.simpleName.asString())
                MapToAnnotationData(
                    target = targetType.declaration as KSClassDeclaration,
                    direction = direction
                )
            }
    }

    private fun KSClassDeclaration.getPropertiesToMap(): List<PropertyData> {
        return propertiesCache.getOrPut(this) {
            getAllProperties().map { property ->
                PropertyData(
                    property,
                    property.simpleName.asString(),
                    property.type.resolve()
                )
            }.toList()
        }
    }

    private fun KSClassDeclaration.getConstructorParametersToMap(): List<ParameterData> {
        return parametersCache.getOrPut(this) {
            primaryConstructor!!.parameters.map { parameter ->
                ParameterData(
                    parameter,
                    parameter.name!!.asString(),
                    parameter.type.resolve()
                )
            }
        }
    }

}

data class MapToAnnotationData(
    val target: KSClassDeclaration,
    val direction: MappingDirection
)

data class MappingData(
    val from: KSClassDeclaration,
    val to: KSClassDeclaration,
    val mapping: Map<String, String>
)

data class PropertyData(
    val reference: KSPropertyDeclaration,
    val name: String,
    val type: KSType
)

data class ParameterData(
    val reference: KSValueParameter,
    val name: String,
    val type: KSType
)
