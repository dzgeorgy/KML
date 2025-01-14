package dev.dzgeorgy.kameleon.ksp.visitors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import dev.dzgeorgy.kameleon.ksp.AliasesCache
import dev.dzgeorgy.kameleon.lib.MapTo

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
        val properties = classDeclaration.getPropertiesToMap()
        return classDeclaration.getMapToData()
            .map { mapToData ->
                val parameters = mapToData.target.getConstructorParametersToMap()
                MappingData(
                    from = classDeclaration,
                    to = mapToData.target,
                    mapping = associateParametersWithProperties(parameters, properties)
                )
            }
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
                MapToAnnotationData(
                    target = targetType.declaration as KSClassDeclaration
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
    val target: KSClassDeclaration
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
