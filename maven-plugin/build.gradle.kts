@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.benediktritter.maven.plugin.development.MavenPluginDevelopmentExtension
import de.benediktritter.maven.plugin.development.internal.DefaultMavenPluginDevelopmentExtension
import de.benediktritter.maven.plugin.development.task.GenerateHelpMojoSourcesTask
import de.benediktritter.maven.plugin.development.task.GenerateMavenPluginDescriptorTask
import org.gradle.api.internal.artifacts.transform.UnzipTransform
import java.util.*

plugins {
    java
    groovy
    `maven-publish`
    `jvm-test-suite`
    alias(libs.plugins.mavenPluginDevelopment)
    alias(libs.plugins.shadow)
}

val ourArtifactId = "cucumber-companion-plugin"

mavenPlugin {
    artifactId.set(ourArtifactId)
}

val m2Repository = layout.buildDirectory.dir("m2")

publishing {
    publications {
        val maven by creating(MavenPublication::class) {
            artifactId = ourArtifactId
//            from(components["java"])
        }
        shadow.component(maven)
    }
    repositories {
        maven {
            name = "testLocal"
            url = m2Repository.get().dir("repository").asFile.toURI()
        }
    }
}



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val mavenInstallation: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    resolutionStrategy.cacheDynamicVersionsFor(1, TimeUnit.HOURS)
    attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
}

val mavenVersion = "3.8.6"

dependencies {
    implementation(projects.companionGenerator)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.pluginApi)
    compileOnly(libs.maven.pluginAnnotations)

    registerTransform(UnzipTransform::class) {
        from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE)
        to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
    }
    mavenInstallation("org.apache.maven:apache-maven:${mavenVersion}") {
        artifact {
            classifier = "bin"
            type = "zip"
            isTransitive = false
        }
    }
}

val mavenInstallDir: Provider<Directory> = (layout.dir(provider { mavenInstallation.singleFile.listFiles()?.single() }))
val takariResourceDir: Provider<Directory> = layout.buildDirectory.dir("takari-test")


val prepareTakariTestProperties by tasks.creating {
    dependsOn("publishMavenPublicationToTestLocalRepository")
    notCompatibleWithConfigurationCache("prototyping")
    outputs.upToDateWhen { false } // TODO needs input tracking
    outputs.dir(takariResourceDir)
    doFirst {

        val testProperties = Properties()
        testProperties.putAll(
            mapOf(
                "project.groupId" to project.group,
                "project.version" to project.version,
                "project.artifactId" to ourArtifactId, // TODO set the publication properly
                "localRepository" to m2Repository.get().dir("repository").asFile.path,
                "repository.0" to "<id>central</id><url>https://repo.maven.apache.org/maven2</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots>",
                "updateSnapshots" to "false"
            )
        )
        takariResourceDir.get().file("test.properties").asFile.writer().use { testProperties.store(it, "") }
    }
}

sourceSets.test.configure {
    output.dir(mapOf("builtBy" to prepareTakariTestProperties), takariResourceDir)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock(libs.versions.spock)
            dependencies {
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
                implementation(libs.takariIntegrationTesting)
            }
            targets {
                all {
                    testTask {
                        // Takari needs at least Java 11
                        javaLauncher.set(javaToolchains.launcherFor {
                            languageVersion = JavaLanguageVersion.of(17)
                        })
                        options {
                            jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-DtestInternal.mavenInstallDir=${mavenInstallDir.get().asFile.absolutePath}") })
                        }
                        systemProperty("testInternal.pluginVersion", version)
                        environment("CONTINUOUS_INTEGRATION", true) // takari will print the log on error if this is set
                    }
                }
            }
        }
    }
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
