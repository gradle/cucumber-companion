enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

val isCI = System.getenv("CI")?.toBoolean() ?: false
val isCC = gradle.startParameter.isConfigurationCacheRequested

require(!isCC || !isCI) { "Configuration-Cache should be disabled on CI" }

rootProject.name = "cucumber-companion"
include("gradle-plugin")
include("maven-plugin")
include("companion-generator")


