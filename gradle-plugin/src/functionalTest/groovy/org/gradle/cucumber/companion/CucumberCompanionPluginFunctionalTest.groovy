/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package org.gradle.cucumber.companion

import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Path

class CucumberCompanionPluginFunctionalTest extends Specification {

    @TempDir
    FileSystemFixture workspace
    def buildFile
    def settingsFile


    @Delegate
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    def "companion task can be registered"() {
        given:
        setupPlugin(buildScriptLanguage)

        when:
        def result = run("tasks", "--all")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        where:
        buildScriptLanguage << ['groovy', 'kotlin']
    }


    def "testGenerateCucumberSuiteCompanion generates valid companion file"() {
        given:
        setupPlugin(buildScriptLanguage)
        createFeatureFiles(workspace)

        when:
        def result = run("testGenerateCucumberSuiteCompanion")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        def expectedCompanions = expectedCompanionFiles()

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }


        where:
        buildScriptLanguage << ['groovy', 'kotlin']
    }

    private String safeName(String name) {
        name.replaceAll(" ", "_")
    }

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("build/generated-sources/cucumberCompanion-test/${companion.relativePath}")
    }

    void setupPlugin(String language) {
        switch (language) {
            case 'groovy':
                setupPluginGroovy()
                break
            case 'kotlin':
                setupPluginKotlin()
                break
            default:
                throw new IllegalArgumentException("Unsupported language: $language")
        }
    }

    private void setupPluginGroovy() {
        buildFile = workspace.file("build.gradle")
        settingsFile = workspace.file("settings.gradle")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                id('java')
                id('jvm-test-suite')
                id('org.gradle.cucumber.companion')
            }

            testing {
                suites {
                    test {
                        // unfortunately we can't make use of Groovy's extension functions, as Gradle doesn't support that.
                        org.gradle.kotlin.dsl.CucumberCompanionKt.generateCucumberSuiteCompanion(delegate, project)
                    }
                }
            }
            """.stripIndent(true)
    }

    private void setupPluginKotlin() {
        buildFile = workspace.file("build.gradle.kts")
        settingsFile = workspace.file("settings.gradle.kts")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                java
                `jvm-test-suite`
                id("org.gradle.cucumber.companion")
            }

            testing {
                suites {
                    val test by getting(JvmTestSuite::class)   {
                        generateCucumberSuiteCompanion(project)
                    }
                }
            }
            """.stripIndent(true)
    }

    BuildResult run(String... arguments) {
        def runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(workspace.currentPath.toFile())
            .withArguments(arguments)

        return runner.build()
    }
}
