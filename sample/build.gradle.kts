plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

application {
    mainClass = "dev.dzgeorgy.kameleon.sample.SimpleKt"
}

dependencies {
    implementation(projects.lib)
    ksp(projects.ksp)
}
