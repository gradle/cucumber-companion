import org.gradle.kotlin.dsl.support.serviceOf

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity").version("3.19.2")
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

val isCI = providers.environmentVariable("CI").presence()
val isCC = gradle.serviceOf<BuildFeatures>().configurationCache.active.getOrElse(false)

require(!isCC || isCI.not().get() ) { "Configuration-Cache should be disabled on CI" }

develocity {
    server = "https://ge.gradle.org"
    buildScan {
        uploadInBackground = isCI.not()
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
        val hasAccessKey = providers.environmentVariable("DEVELOCITY_ACCESS_KEY").map { it.isNotBlank() }.orElse(false)
        isPush = hasAccessKey.zip(isCI) { accessKey, ci -> ci && accessKey }.get()
    }
}

rootProject.name = "cucumber-companion"

include("gradle-plugin")
include("maven-plugin")
include("companion-generator")

fun Provider<*>.presence(): Provider<Boolean> = map { true }.orElse(false)
fun Provider<Boolean>.not(): Provider<Boolean> = map { !it }
