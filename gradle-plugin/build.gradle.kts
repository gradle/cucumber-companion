@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    groovy
    `kotlin-dsl`
    alias(libs.plugins.gradlePluginPublish)
    id("conventions.publishing")
    id("conventions.test-context")
    id("conventions.verify-publication")
}

project.description = "Gradle Plugin making Cucumber tests compatible with Gradle Enterprise test acceleration features"
val gradlePluginArtifactId = "cucumber-companion-gradle-plugin"

base.archivesName.set(gradlePluginArtifactId)

afterEvaluate {
    val pluginMaven by publishing.publications.getting(MavenPublication::class) {
        artifactId = gradlePluginArtifactId
    }
}

val crossVersions = listOf(
    CrossVersionTest(libs.versions.gradleMinSupported.get(), false), // lowest supported versions as jvm-test-suites was added here
    CrossVersionTest(GradleVersion.current().version, true),
)

testContext {
    mappings = mapOf(
        "junitVersion" to libs.versions.junit.get(),
        "cucumberVersion" to libs.versions.cucumber.get()
    )
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier = ""
    from(file("../LICENSE")) {
        into("META-INF")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(projects.companionGenerator)
}

verifyPublication {
    expectPublishedArtifact("cucumber-companion-gradle-plugin") {
        withClassifiers("", "javadoc", "sources")
        // dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()
        withJarContaining {
            // Test for shadowed files
            aFile("org/gradle/cucumber/companion/generator/CompanionGenerator.class")
            aFile("META-INF/LICENSE")
        }
    }
    expectPublishedArtifact("org.gradle.cucumber.companion.gradle.plugin") {
        withPomFileMatchingMavenCentralRequirements()
    }
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

val allCrossVersionTests = crossVersions.map {
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
        displayName = "Cucumber Companion Plugin"
        description = project.description
        tags.addAll("cucumber", "test")
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
