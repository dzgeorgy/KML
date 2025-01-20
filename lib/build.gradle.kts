plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    // Targets
    jvm()

    // Config
    jvmToolchain(11)
}
