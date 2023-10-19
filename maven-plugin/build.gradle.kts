@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.benediktritter.maven.plugin.development.MavenPluginDevelopmentExtension
import de.benediktritter.maven.plugin.development.task.GenerateHelpMojoSourcesTask
import de.benediktritter.maven.plugin.development.task.GenerateMavenPluginDescriptorTask

plugins {
    java
    groovy
    `maven-publish`
    `jvm-test-suite`
    alias(libs.plugins.mavenPluginDevelopment)
    id("conventions.maven-plugin-testing")
    id("conventions.publishing")
    id("conventions.test-context")
    id("conventions.verify-publication")
    id("conventions.code-style")
}

project.description = "Maven Plugin making Cucumber tests compatible with Gradle Enterprise test acceleration features"
val mavenPluginArtifactId = "cucumber-companion-maven-plugin"

mavenPlugin {
    artifactId.set(mavenPluginArtifactId)
    dependencies = configurations.named("shadow")
}

verifyPublication {
    expectPublishedArtifact("cucumber-companion-maven-plugin") {
        withClassifiers("", "javadoc", "sources")
        // dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()

        withJarContaining {
            aFile("META-INF/maven/plugin.xml")
            aFile("META-INF/maven/com.gradle.cucumber.companion/maven-plugin/plugin-help.xml") {
                matching("Should contain plugin's artifact id") { it.contains("<artifactId>cucumber-companion-maven-plugin</artifactId>") }
            }
            // Test for shadowed files
            aFile("org/gradle/cucumber/companion/generator/CompanionGenerator.class")
            aFile("META-INF/LICENSE")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

val mavenJava by publishing.publications.creating(MavenPublication::class) {
    artifactId = mavenPluginArtifactId
    artifact(tasks.named("javadocJar"))
    artifact(tasks.named("sourcesJar"))
    shadow.component(this)
}

dependencies {
    implementation(projects.companionGenerator)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.pluginApi)
    compileOnly(libs.maven.pluginAnnotations)
}

val functionalTest by testing.suites.getting(JvmTestSuite::class) {
    useSpock(libs.versions.spock)
    dependencies {
        implementation(platform(libs.groovy.bom.get().toString()))
        implementation(libs.groovy.nio)
        implementation(libs.groovy.xml)
        implementation(testFixtures(projects.companionGenerator))
    }
}

testContext {
    mappings = mapOf(
        "junitVersion" to libs.versions.junit.get(),
        "cucumberVersion" to libs.versions.cucumber.get(),
        "surefireVersion" to libs.versions.surefire.get(),
        "failsafeVersion" to libs.versions.failsafe.get()
    )
}

mavenPluginTesting {
    mavenVersions = setOf(
        libs.versions.mavenMinCompatible.get(),
        "3.8.7",
        libs.versions.mavenMaxCompatible.get()
    )
    pluginPublication = mavenJava
}

// adapted from the mavenPluginDevelopment plugin, otherwise the shadowJar doesn't pickup the necessary metadata files
project.afterEvaluate {
    val sourceSet = extensions.getByType(MavenPluginDevelopmentExtension::class).pluginSourceSet.get()
    tasks.named<ShadowJar>("shadowJar").configure {
        from(tasks.named<GenerateMavenPluginDescriptorTask>("generateMavenPluginDescriptor"))
        from(file("../LICENSE")) {
            into("META-INF")
        }
    }
    sourceSet.java.srcDir(
        tasks.named<GenerateHelpMojoSourcesTask>("generateMavenPluginHelpMojoSources").map { it.outputDirectory })
}

listOf("generateMavenPluginDescriptor", "generateMavenPluginHelpMojoSources").forEach {
    tasks.named(it) {
        notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }
}
