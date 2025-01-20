plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    // Targets
    jvm()

    // Config
    jvmToolchain(11)

    // Dependencies
    sourceSets.commonMain.dependencies {
        // Projects
        implementation(projects.lib)

        // Libraries
        implementation(libs.ksp.api)
        implementation(libs.kotlinpoet)
    }
}
