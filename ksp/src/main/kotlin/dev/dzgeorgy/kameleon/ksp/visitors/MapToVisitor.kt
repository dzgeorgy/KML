package dev.dzgeorgy.kameleon.ksp.visitors

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import dev.dzgeorgy.kameleon.ksp.data.MappingData
import dev.dzgeorgy.kameleon.lib.MapTo

class MapToVisitor(
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val results = mutableListOf<MappingData>()

    // Cache.
    private val propertiesCache: MutableMap<KSClassDeclaration, Map<String, KSType>> = hashMapOf()
    private val parametersCache: MutableMap<KSClassDeclaration, Map<String, KSType>> = hashMapOf()

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {

        // Resolve annotations.
        val annotations = classDeclaration.annotations.filter {
            it.shortName.asString() == MapTo::class.simpleName
        }

        // Process annotations.
        for (annotation in annotations) {

            // Get target from annotation parameter.
            val target = annotation.arguments.firstOrNull {
                it.name?.asString() == MapTo::target.name
            }?.value as? KSType ?: run {
                logger.error("Unable to get ${MapTo::target.name}.", classDeclaration)
                return
            }

            // Resolve class of target.
            val targetClass = target.declaration.closestClassDeclaration() ?: run {
                logger.error("Unable to get class declaration for $target.", classDeclaration)
                return
            }

            // Get source properties.
            val properties = classDeclaration.getPropertiesMap()

            // Get target parameters.
            val parameters = targetClass.getConstructorParametersMap()

            // Find matching property for each parameter.
            val result = parameters.map { (paramName, paramType) ->
                val property = properties.entries.firstOrNull { (propName, propType) ->
                    isMatchingProperty(propName to propType, paramName to paramType)
                } ?: run {
                    logger.error("Unable to find matching property for $paramName: $paramType in $target", classDeclaration)
                    return
                }
                paramName to property.key
            }.toMap()

            // Build result
            results.add(
                MappingData(
                    source = classDeclaration,
                    target = targetClass,
                    mappedData = result
                )
            )

        }
    }

    private fun isMatchingProperty(prop: Pair<String, KSType>, param: Pair<String, KSType>): Boolean {
        return param.first == prop.first && prop.second.isAssignableFrom(param.second)
    }

    private fun KSClassDeclaration.getPropertiesMap() = propertiesCache.getOrPut(this) {
        getAllProperties().associate {
            it.simpleName.asString() to it.type.resolve()
        }
    }

    private fun KSClassDeclaration.getConstructorParametersMap() = parametersCache.getOrPut(this) {
        primaryConstructor!!.parameters.associate {
            it.name!!.asString() to it.type.resolve()
        }
    }

    fun getResults(): List<MappingData> = results

}
