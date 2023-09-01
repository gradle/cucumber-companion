@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.internal.artifacts.transform.UnzipTransform
import java.util.*

plugins {
    id("java")
    id("maven-publish")
    id("jvm-test-suite")
    id("conventions.maven-distributions")
}

// hack to make the version catalog available to convention plugin scripts (https://github.com/gradle/gradle/issues/17968)
val libs = the<LibrariesForLibs>()

interface MavenPluginTestingExtension {
    val mavenVersions: SetProperty<String>
}

val extension = extensions.create<MavenPluginTestingExtension>("mavenPluginTesting")

val m2Repository = layout.buildDirectory.dir("m2")
val takariResourceDir: Provider<Directory> = layout.buildDirectory.dir("takari-test")

val prepareTakariTestProperties by tasks.creating {
    // installs our own plugin into the project-local m2 repository in ./build/m2
    dependsOn("publishMavenPublicationToTestLocalRepository")

    val publication = provider { publishing.publications.withType<MavenPublication>().single() }

    notCompatibleWithConfigurationCache("prototyping")
    outputs.dir(takariResourceDir)
    inputs.property("groupId", publication.map { it.groupId })
    inputs.property("artifactId", publication.map { it.artifactId })
    inputs.property("version", publication.map { it.version })

    doFirst {
        val testProperties = Properties()
        testProperties.putAll(
            mapOf(
                "project.groupId" to publication.map { it.groupId }.get(),
                "project.artifactId" to publication.map { it.artifactId }.get(),
                "project.version" to publication.map { it.version }.get(),
                "localRepository" to m2Repository.get().dir("repository").asFile.path,
                "repository.0" to "<id>central</id><url>https://repo.maven.apache.org/maven2</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots>",
                "updateSnapshots" to "false"
            )
        )
        takariResourceDir.get().file("test.properties").asFile.writer().use { testProperties.store(it, "") }
    }
}

// Create a project-local file system m2 repository that will be used for all functional tests.
publishing {
    repositories {
        maven {
            name = "testLocal"
            url = m2Repository.get().dir("repository").asFile.toURI()
        }
    }
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    testType = TestSuiteType.FUNCTIONAL_TEST
    dependencies {
        implementation(libs.takariIntegrationTesting)
    }
    // add takari test.properties to classpath
    sources {
        output.dir(mapOf("builtBy" to prepareTakariTestProperties), takariResourceDir)
    }
    targets {
        all {
            testTask {
                outputs.dir(m2Repository)
                doFirst {
                    m2Repository.get().asFile.mkdirs()
                }
                // Takari needs at least Java 11
                javaLauncher.set(javaToolchains.launcherFor {
                    languageVersion = JavaLanguageVersion.of(17)
                })
                environment("CONTINUOUS_INTEGRATION", true) // takari will print the log on error if this is set
                options {
                    val mavenDistributions = project.the<MavenDistributionExtension>()
                    extension.mavenVersions.get().forEach { mavenVersion ->
                        val mavenDistribution = mavenDistributions.versions.maybeCreate(mavenVersion)
                        jvmArgumentProviders += mavenDistribution
                    }
                }
            }
        }
    }
}
