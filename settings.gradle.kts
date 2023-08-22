enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "cucumber-companion"
include("gradle-plugin")
include("maven-plugin")
include("companion-generator")
