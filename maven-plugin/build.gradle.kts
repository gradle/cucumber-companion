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
    `java-test-fixtures`
    alias(libs.plugins.mavenPluginDevelopment)
    alias(libs.plugins.shadow)
    id("conventions.maven-plugin-testing")
}

val ourArtifactId = "cucumber-companion-plugin"

mavenPlugin {
    artifactId.set(ourArtifactId)
}

publishing {
    publications {
        val maven by creating(MavenPublication::class) {
            artifactId = ourArtifactId
            //from(components["java"])
        }
        shadow.component(maven)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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

mavenPluginTesting {
    mavenVersions = setOf("3.8.6", "3.8.7", "3.9.1")
    pluginPublication = publishing.publications.named<MavenPublication>("maven")
}


// adapted from the mavenPluginDevelopment plugin, otherwise the shadowJar doesn't pickup the necessary metadata files
project.afterEvaluate {
    val sourceSet = extensions.getByType(MavenPluginDevelopmentExtension::class).pluginSourceSet.get()
    tasks.named<ShadowJar>("shadowJar").configure {
        from(tasks.named<GenerateMavenPluginDescriptorTask>("generateMavenPluginDescriptor"))
    }
    sourceSet.java.srcDir(tasks.named<GenerateHelpMojoSourcesTask>("generateMavenPluginHelpMojoSources").map { it.outputDirectory })
}

listOf("generateMavenPluginDescriptor", "generateMavenPluginHelpMojoSources").forEach {
    tasks.named(it) {
        notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }
}
