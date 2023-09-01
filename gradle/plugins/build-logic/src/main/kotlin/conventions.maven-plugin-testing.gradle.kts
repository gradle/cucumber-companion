@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs
import java.util.*

plugins {
    id("java")
    id("maven-publish")
    id("jvm-test-suite")
    id("conventions.maven-distributions")
}

// hack to make the version catalog available to convention plugin scripts (https://github.com/gradle/gradle/issues/17968)
val libs = the<LibrariesForLibs>()

abstract class MavenPluginTestingExtension {

    companion object {
        const val TEST_REPOSITORY_NAME = "TestLocal"
    }

    abstract val mavenVersions: SetProperty<String>
    abstract val pluginPublication: Property<MavenPublication>

    fun publishToTestRepositoryTaskName(): Provider<String> {
        return pluginPublication.map { it.name }
            .map { it.replaceFirstChar(Char::titlecase) }
            .map { "publish${it}PublicationTo${TEST_REPOSITORY_NAME}Repository" }
    }
}

val extension = extensions.create<MavenPluginTestingExtension>("mavenPluginTesting")

val m2Repository: Provider<Directory> = layout.buildDirectory.dir("m2")
val takariResourceDir: Provider<Directory> = layout.buildDirectory.dir("takari-test")

val prepareTakariTestProperties by tasks.creating(TakariTestPropertiesTask::class) {
    dependsOn(extension.publishToTestRepositoryTaskName())

    groupId = extension.pluginPublication.map { it.groupId }
    artifactId = extension.pluginPublication.map { it.artifactId }
    version = extension.pluginPublication.map { it.version }
    testRepositoryPath = m2Repository
    outputDirectory = takariResourceDir
}

// Create a project-local file system m2 repository that will be used for all functional tests.
publishing {
    repositories {
        maven {
            name = MavenPluginTestingExtension.TEST_REPOSITORY_NAME
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
