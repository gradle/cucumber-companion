@file:Suppress("UnstableApiUsage")

import org.gradle.api.internal.artifacts.transform.UnzipTransform
import java.util.*

plugins {
    java
    groovy
    `maven-publish`
    `jvm-test-suite`
    alias(libs.plugins.mavenPluginDevelopment)
}

val ourArtifactId = "cucumber-companion-plugin"

mavenPlugin {
    artifactId.set(ourArtifactId)
}

val m2Repository = layout.buildDirectory.dir("m2")

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = ourArtifactId
            from(components["java"])
        }
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

val mavenVersion = "3.3.9"

dependencies {
    implementation(projects.companionGenerator)
    implementation(libs.maven.pluginApi)
    implementation(libs.maven.pluginAnnotations)

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
                        doFirst {
                            m2Repository.get().asFile.mkdirs()
                        }
                        // Takari needs at least Java 11
                        javaLauncher.set(javaToolchains.launcherFor {
                            languageVersion = JavaLanguageVersion.of(17)
                        })
                        options {
                            jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-DtestInternal.mavenInstallDir=${mavenInstallDir.get().asFile.absolutePath}") })
                        }
                    }
                }
            }
        }
    }
}

listOf("generateMavenPluginDescriptor", "generateMavenPluginHelpMojoSources").forEach {
    tasks.named(it) {
        notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }
}
