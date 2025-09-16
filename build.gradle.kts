plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
    alias(libs.plugins.ksp)

    alias(libs.plugins.kotlinx.serialization)
}

allprojects {
    group = "me.santio"
    version = "0.0.1-BETA"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
    }

    apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)

    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    api(project(":api"))

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    compileOnly(libs.autoservice.google)
    ksp(libs.autoservice.ksp)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.bundles.cloud)
    implementation(libs.objenesis)
    implementation(libs.slf4j.api)
    implementation(libs.nats)
    implementation(libs.zstd)
    implementation(libs.packetevents)
    implementation(libs.minio) {
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
}

tasks.compileKotlin {
    compilerOptions {
        freeCompilerArgs.add("-java-parameters")
    }
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("Nexus-${project.version}.jar")
}

tasks.build.get().dependsOn(tasks.shadowJar)


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}