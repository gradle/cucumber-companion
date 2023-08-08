dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "cucumber-companion"
include("gradle-plugin")
include("maven-plugin")
include("companion-generator")
