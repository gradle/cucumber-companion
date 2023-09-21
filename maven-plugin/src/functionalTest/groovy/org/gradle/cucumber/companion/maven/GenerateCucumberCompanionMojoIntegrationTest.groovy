package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper
import org.gradle.cucumber.companion.fixtures.CucumberFeature

import java.nio.file.Files

class GenerateCucumberCompanionMojoIntegrationTest extends BaseCucumberCompanionMavenFuncTest {

    def "should not fail if resources folder does not exist"() {
        given:
        createProject()

        when:
        maven.execute(workspace, "test")

        then:
        noExceptionThrown()
    }

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
        def succeedingFeatures = CucumberFeature.allSucceeding()
        createProject()
        createFeatureFiles(workspace.fileSystem, succeedingFeatures)
        createStepFiles(workspace.fileSystem, succeedingFeatures)

        when:
        def result = maven.execute(workspace, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles("Test", succeedingFeatures)
        expectedCompanions.forEach {
            verifyAll(sureFireTestReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }
    }

    def "can run failing cucumber tests"() {
        given:
        def failingFeatures = [CucumberFeature.FailingFeature]
        createProject()
        createFeatureFiles(workspace.fileSystem, failingFeatures)
        createStepFiles(workspace.fileSystem, failingFeatures)

        when:
        maven.execute(workspace, "test")

        then:
        thrown(Exception)

        and:
        def log = workspace.fileSystem.file("log.txt").text
        log.find("Failing Feature.A feature which does not succeed when executed -- Time elapsed: .+ <<< FAILURE!") != null
    }


}
