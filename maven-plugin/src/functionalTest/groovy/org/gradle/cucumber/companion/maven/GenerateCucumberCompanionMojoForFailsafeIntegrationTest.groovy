package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper

import java.nio.file.Files

class GenerateCucumberCompanionMojoForFailsafeIntegrationTest extends BaseCucumberCompanionMavenFuncTest {

    def "generate-cucumber-companion-files mojo generates valid companion file"() {
        given:
        createProject()
        configureCompanionPluginForFailsafe()
        createFeatureFiles(workspace.fileSystem)
        createStepFiles(workspace.fileSystem)

        when:
        def result = maven.execute(workspace, "generate-test-sources")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles("IT")

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }
    }

    def "generate-cucumber-companion-files mojo generates valid companion files that are picked up by surefire"() {
        given:
        createProject()
        configureCompanionPluginForFailsafe()
        createFeatureFiles(workspace.fileSystem)
        createStepFiles(workspace.fileSystem)

        when:
        def result = maven.execute(workspace, "verify")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles("IT")
        expectedCompanions.forEach {
            verifyAll(failsafeFireTestReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }
    }

    private void configureCompanionPluginForFailsafe() {
        workspace.pom.replacePlugin("org.gradle.cucumber.companion", "cucumber-companion-maven-plugin", '${it-project.version}') {
            executions {
                execution {
                    goals {
                        goal("generate-cucumber-companion-files")
                    }
                    configuration {
                        companionSuffix("IT")
                    }
                }
            }
        }
    }
}
