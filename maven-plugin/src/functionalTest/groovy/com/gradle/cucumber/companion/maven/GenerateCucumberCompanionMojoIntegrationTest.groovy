/*
 * Copyright 2025 the original author or authors.
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

import com.gradle.cucumber.companion.fixtures.CucumberFeature
import groovy.xml.XmlSlurper

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
        if (baseClass) {
            createBaseClass(workspace.fileSystem, baseClass)
        }
        interfaces.each {
            createInterface(workspace.fileSystem, it)
        }
        configureCompanionPlugin {
            customizeGeneratedClasses {
                if (baseClass) {
                    delegate."baseClass"(baseClass)
                }
                if (interfaces) {
                    delegate."interfaces" {
                        interfaces.forEach {
                            "interface"(it)
                        }
                    }
                }
                if (annotations) {
                    delegate."annotations" {
                        annotations.forEach {
                            annotation(it)
                        }
                    }
                }
            }
        }

        when:
        def result = maven.execute(workspace, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles(suffix: "Test", baseClass: baseClass, interfaces: interfaces, annotations: annotations)

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        baseClass   | interfaces                    | annotations
        null        | []                            | []
        "base.Base" | []                            | []
        null        | ["base.IFace"]                | []
        null        | ["base.IFace", "base.IOther"] | []
        null        | []                            | ['@org.junit.jupiter.api.Tag("tag")']
        null        | []                            | ['@org.junit.jupiter.api.Tag("tag1")', '@org.junit.jupiter.api.Tag("tag2")']
        "base.Base" | ["base.IFace"]                | ['@org.junit.jupiter.api.Tag("tag")']
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
        def expectedCompanions = expectedCompanionFiles(suffix: "Test", features: succeedingFeatures)
        expectedCompanions.forEach {
            verifyAll(sureFireTestReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }
    }

    def "generate-cucumber-companion-files mojo generates valid companion file allowing for empty suites"() {
        given:
        def allSucceedingFeatures = CucumberFeature.allSucceeding()
        def discoveredSucceedingFeatures = [CucumberFeature.USER_PROFILE]
        createProject()
        configureCompanionPluginToAllowEmptySuites()
        createFeatureFiles(workspace.fileSystem)
        createStepFiles(workspace.fileSystem)
        createPostDiscoveryFilter(workspace.fileSystem, "$CucumberFeature.USER_PROFILE.packageName.$CucumberFeature.USER_PROFILE.className")
        registerPostDiscoveryFilter(workspace.fileSystem)

        when:
        def result = maven.execute(workspace, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println(it) }

        and:
        def expectedCompanions = expectedCompanionFiles(suffix: "Test", allowEmptySuites: true, features: allSucceedingFeatures)

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
            def expectedTestRuns = discoveredSucceedingFeatures
                .collect { f -> f.featureName }
                .contains(it.featureName) ? 1 : 0
            verifyAll(sureFireTestReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == expectedTestRuns
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
        log.find("A feature which does not succeed when executed -- Time elapsed: .+ <<< FAILURE!") != null
    }

}
