pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    val mavenPublishVersion: String by settings
    val ktfmtVersion: String by settings
    val detektVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("com.vanniktech.maven.publish") version mavenPublishVersion
        id("com.ncorti.ktfmt.gradle") version ktfmtVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
    }
}

rootProject.name = "ksv"
