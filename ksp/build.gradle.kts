plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.lib)
    implementation(libs.kotlinpoet)
}
