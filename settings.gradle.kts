import org.gradle.kotlin.dsl.support.serviceOf

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version ("3.17")
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.13"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
val isCI = System.getenv("CI")?.toBoolean() ?: false
val isCC = gradle.serviceOf<BuildFeatures>().configurationCache.active.getOrElse(false)

require(!isCC || !isCI) { "Configuration-Cache should be disabled on CI" }

rootProject.name = "cucumber-companion"
include("gradle-plugin")
include("maven-plugin")
include("companion-generator")

develocity {
    buildScan {
        publishing.onlyIf { false }
    }
}
