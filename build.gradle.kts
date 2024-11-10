import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
    signing
    id("org.jetbrains.dokka")
    id("com.ncorti.ktfmt.gradle")
    id("io.gitlab.arturbosch.detekt")
}

group = "io.github.clasicrando"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }
    linuxX64()
    linuxArm64()
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.5.4")
            }


        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktfmt {
    kotlinLangStyle()
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
}
