package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper
import org.gradle.maven.functest.JDK
import org.gradle.maven.functest.MavenDistribution

import java.nio.file.Files

class GenerateCucumberCompanionMojoIntegrationTest extends BaseCucumberCompanionMavenFuncTest {

    static final TestMatrix MATRIX = TestMatrix.of(
        JDK.ifAvailable(8, 11, 17),
        MavenDistribution.allDistributions(),
        ["3.1.2"])

    def "generate-cucumber-companion-files mojo generates valid companion file"() {
        given:
        createProject() {
            plugin("org.apache.maven.plugins", "maven-surefire-plugin", testCase.surefireVersion)
        }
        cucumberFixture.createFeatureFiles(workspace.fileSystem)
        cucumberFixture.createStepFiles(workspace.fileSystem)

        when:
        def result = testCase.maven.execute(workspace, testCase.javaVersion, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()

        and:
        def expectedCompanions = cucumberFixture.expectedCompanionFiles("Test")

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        testCase << MATRIX.executions()
    }

    def "generate-cucumber-companion-files mojo generates valid companion files that are picked up by surefire"() {
        given:
        createProject()
        cucumberFixture.createFeatureFiles(workspace.fileSystem)
        cucumberFixture.createStepFiles(workspace.fileSystem)

        when:
        def result = testCase.maven.execute(workspace, JDK.current(), "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()

        and:
        def expectedCompanions = cucumberFixture.expectedCompanionFiles("Test")
        expectedCompanions.forEach {
            verifyAll(testReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }

        where:
        testCase << MATRIX.executions()
    }

}
