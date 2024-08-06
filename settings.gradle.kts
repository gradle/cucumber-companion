import org.gradle.kotlin.dsl.support.serviceOf

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity").version("3.17.6")
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.0.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

val isCI = System.getenv("CI")?.toBoolean() ?: false
val isCC = gradle.serviceOf<BuildFeatures>().configurationCache.active.getOrElse(false)

require(!isCC || !isCI) { "Configuration-Cache should be disabled on CI" }

develocity {
    server = "https://ge.gradle.org"
    buildScan {
        uploadInBackground = !isCI
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
        }
    }
}

buildCache {
    local {
        isEnabled = true
    }

    remote(develocity.buildCache) {
        server = "https://eu-build-cache.gradle.org"
        isEnabled = true
        val accessKey = providers.environmentVariable("DEVELOCITY_ACCESS_KEY").orNull
        isPush = isCI && !accessKey.isNullOrEmpty()
    }
}

rootProject.name = "cucumber-companion"

include("gradle-plugin")
include("maven-plugin")
include("companion-generator")
