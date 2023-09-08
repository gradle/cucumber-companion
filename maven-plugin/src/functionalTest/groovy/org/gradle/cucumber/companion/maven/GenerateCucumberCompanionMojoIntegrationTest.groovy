package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper

import java.nio.file.Files

class GenerateCucumberCompanionMojoIntegrationTest extends BaseCucumberCompanionMavenFuncTest {

    def "generate-cucumber-companion-files mojo generates valid companion file"() {
        given:
        createProject()
        createFeatureFiles(workspace.fileSystem)
        createStepFiles(workspace.fileSystem)

        when:
        def result = maven.execute(workspace, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles("Test")

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }
    }

    def "generate-cucumber-companion-files mojo generates valid companion files that are picked up by surefire"() {
        given:
        createProject()
        createFeatureFiles(workspace.fileSystem)
        createStepFiles(workspace.fileSystem)

        when:
        def result = maven.execute(workspace, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles("Test")
        expectedCompanions.forEach {
            verifyAll(testReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }
    }

}
