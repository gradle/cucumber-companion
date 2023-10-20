/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper
import com.gradle.cucumber.companion.fixtures.CucumberFeature

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
        def failingFeatures = [CucumberFeature.FAILING_FEATURE]
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
