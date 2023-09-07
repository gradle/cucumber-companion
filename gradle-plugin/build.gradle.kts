@file:Suppress("UnstableApiUsage")

plugins {
    groovy
    `java-gradle-plugin`
    `java-test-fixtures`
    alias(libs.plugins.shadow)
    kotlin("jvm") version libs.versions.kotlin
}

val crossVersions = listOf(
    CrossVersionTest("7.3", false), // lowest supported versions as jvm-test-suites was added here
    CrossVersionTest(GradleVersion.current().version, true),
)

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

val functionalTestTask = tasks.named<Test>("functionalTest") {
    jvmArgumentProviders.add(CrossVersionTest(GradleVersion.current().version, false))
}

val allCrossVersionTests =  crossVersions.map {
    tasks.register<Test>("functionalTest_${it.testName}") {
        val templateTask = functionalTestTask.get()
        classpath = templateTask.classpath
        testClassesDirs = templateTask.testClassesDirs
        useJUnitPlatform()
        group = "Cross Version"
        description = "Test with Gradle $it ${if (it.configurationCache) "and configuration cache" else ""}"
        jvmArgumentProviders.add(it)
    }
}

tasks.register("allGradleCrossVersionTests") {
    dependsOn(allCrossVersionTests)
    group = "Cross Version"
    description = "Runs all ${allCrossVersionTests.size} gradle cross version test tasks"
}

gradlePlugin {
    // Define the plugin
    val cucumberCompanion by plugins.creating {
        id = "org.gradle.cucumber.companion"
        implementationClass = "org.gradle.cucumber.companion.CucumberCompanionPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

data class CrossVersionTest(@Input val gradleVersion: String, @Input val configurationCache: Boolean) :
    CommandLineArgumentProvider {
    @get:Internal
    val testName: String
        get() {
            val parts = mutableListOf("gradle", gradleVersion)
            if (configurationCache) {
                parts.add("cc")
            }
            return parts.joinToString("_").replace("[^a-zA-Z0-9_]", "_")
        }

    override fun asArguments(): List<String> =
        listOf(
            "-DtestContext.internal.gradleVersion=$gradleVersion",
            "-DtestContext.internal.configurationCache=$configurationCache"
        )
}
