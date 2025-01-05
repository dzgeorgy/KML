package dev.dzgeorgy.kameleon.ksp.data

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class MappingData(
    val source: KSClassDeclaration,
    val target: KSClassDeclaration,
    val mappedData: Map<String, String>
)
