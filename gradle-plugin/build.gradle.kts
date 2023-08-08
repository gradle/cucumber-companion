@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version libs.versions.kotlin
}


testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(libs.versions.kotlin)
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(libs.versions.kotlin)

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }

        tasks.named<Task>("check") {
            dependsOn(functionalTest)
        }
    }
}

gradlePlugin {
    // Define the plugin
    val greeting by plugins.creating {
        id = "org.gradle.cucumber.companion.greeting"
        implementationClass = "org.gradle.cucumber.companion.CucumberCompanionPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
