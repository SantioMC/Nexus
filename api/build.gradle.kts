plugins {
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.kotlinx.serialization.core)
}