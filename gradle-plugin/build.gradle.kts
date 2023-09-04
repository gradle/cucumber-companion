@file:Suppress("UnstableApiUsage")

plugins {
    groovy
    `java-gradle-plugin`
    `java-test-fixtures`
    kotlin("jvm") version libs.versions.kotlin
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(projects.companionGenerator)
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(project())
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
                implementation(testFixtures(projects.companionGenerator))
            }
        }

        tasks.named<Task>("check") {
            dependsOn(functionalTest)
        }
    }
}

gradlePlugin {
    // Define the plugin
    val cucumberCompanion by plugins.creating {
        id = "org.gradle.cucumber.companion"
        implementationClass = "org.gradle.cucumber.companion.CucumberCompanionPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
