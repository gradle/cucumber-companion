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
package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper

import java.nio.file.Files

class GenerateCucumberCompanionMojoForFailsafeIntegrationTest extends BaseCucumberCompanionMavenFuncTest {

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
        workspace.pom.replacePlugin("com.gradle.cucumber.companion", "cucumber-companion-maven-plugin", '${it-project.version}') {
            executions {
                execution {
                    goals {
                        goal("generate-cucumber-companion-files")
                    }
                    configuration {
                        generatedFileNameSuffix("IT")
                    }
                }
            }
        }
    }
}
