/*
 * Copyright 2024 the original author or authors.
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
package com.gradle.cucumber.companion

import com.gradle.cucumber.companion.fixtures.CompanionAssertions
import com.gradle.cucumber.companion.fixtures.CucumberFeature
import com.gradle.cucumber.companion.fixtures.CucumberFixture
import com.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import com.gradle.cucumber.companion.testcontext.TestContext
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Files
import java.nio.file.Path

class CucumberCompanionPluginFunctionalTest extends Specification {

    static final String CUCUMBER_VERSION = TestContext.getRequiredValue("cucumberVersion")
    static final String JUNIT_VERSION = TestContext.getRequiredValue("junitVersion")

    @TempDir
    FileSystemFixture workspace
    def buildFile
    def settingsFile


    @Delegate
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    @Delegate
    TestContextRunner runner

    def setup() {
        runner = new TestContextRunner(workspace.currentPath)
    }

    def "should not fail if resources folder does not exist"(BuildScriptLanguage buildScriptLanguage) {
        given:
        setupPlugin(buildScriptLanguage)

        when:
        def result = run("test")

        then:
        result.task(":testGenerateCucumberSuiteCompanion").outcome == TaskOutcome.NO_SOURCE

        where:
        buildScriptLanguage << BuildScriptLanguage.values()
    }

    def "companion task can be registered"(BuildScriptLanguage buildScriptLanguage) {
        given:
        setupPlugin(buildScriptLanguage)

        when:
        def result = run("tasks", "--all")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        where:
        buildScriptLanguage << BuildScriptLanguage.values()
    }

    def "testGenerateCucumberSuiteCompanion generates valid companion files"(BuildScriptLanguage buildScriptLanguage, Variant variant) {
        given:
        setupPlugin(buildScriptLanguage, variant)
        createFeatureFiles(workspace)

        when:
        def result = run("testGenerateCucumberSuiteCompanion")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        def expectedCompanions = expectedCompanionFiles("", variant == Variant.IMPLICIT_RELAXED)

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), Variant.values()].combinations()
    }

    def "generated companion files are picked up by Gradle's test task and tests succeed"() {
        given:
        def succeedingFeatures = CucumberFeature.allSucceeding()
        setupPlugin(buildScriptLanguage, variant)
        createFeatureFiles(workspace, succeedingFeatures)
        createStepFiles(workspace, succeedingFeatures)

        when:
        def result = run("test")

        then:
        succeedingFeatures
            .collect { it.toExpectedTestTaskOutput("PASSED") }
            .every {
                result.output.contains(it)
            }

        and:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), Variant.values()].combinations()
    }

    def "generated companion files are picked up by Gradle's test task and only discovered tests succeed"() {
        given:
        def allSucceedingFeatures = CucumberFeature.allSucceeding()
        def discoveredSucceedingFeatures = [CucumberFeature.USER_PROFILE]
        setupPlugin(buildScriptLanguage, variant)
        createFeatureFiles(workspace, allSucceedingFeatures)
        createStepFiles(workspace, allSucceedingFeatures)
        createPostDiscoveryFilter(workspace)
        registerPostDiscoveryFilter(workspace)

        when:
        def result = run("test")

        then:
        discoveredSucceedingFeatures
            .collect { it.toExpectedTestTaskOutput("PASSED") }
            .every {
                result.output.contains(it)
            }

        and:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        and:
        allSucceedingFeatures.findAll { !discoveredSucceedingFeatures.contains(it) }
            .collect { it.toExpectedTestTaskOutput("PASSED") }
            .every {
                !result.output.contains(it)
            }

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), [Variant.IMPLICIT_RELAXED]].combinations()
    }

    def "can run failing cucumber test"() {
        def failingFeatures = [CucumberFeature.FAILING_FEATURE]
        setupPlugin(buildScriptLanguage, variant)
        createFeatureFiles(workspace, failingFeatures)
        createStepFiles(workspace, failingFeatures)

        when:
        def result = runAndFail("test")

        then:
        failingFeatures
            .collect { it.toExpectedTestTaskOutput("FAILED") }
            .every {
                result.output.contains(it)
            }

        and:
        result.task(":test").outcome == TaskOutcome.FAILED

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), Variant.values()].combinations()
    }

    def "testGenerateCucumberSuiteCompanion is incremental"(BuildScriptLanguage buildScriptLanguage) {
        given: "starting with a single feature"
        setupPlugin(buildScriptLanguage)
        createFeatureFiles(workspace, [CucumberFeature.PRODUCT_SEARCH])

        when: "running the generate task"
        def result = run("testGenerateCucumberSuiteCompanion")

        then: "feature companion is present"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles('', false, [CucumberFeature.PRODUCT_SEARCH]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "adding another feature"
        createFeatureFiles(workspace, [CucumberFeature.PASSWORD_RESET])

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "both companion files are present"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles('', false, [CucumberFeature.PRODUCT_SEARCH, CucumberFeature.PASSWORD_RESET]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "modifying an existing feature"
        createFeatureFiles(workspace, [CucumberFeature.PASSWORD_RESET_V2])

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "both companion files are present"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles('', false, [CucumberFeature.PRODUCT_SEARCH, CucumberFeature.PASSWORD_RESET_V2]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "deleting a feature file"
        Files.delete(workspace.file("src/test/resources/${CucumberFeature.PRODUCT_SEARCH.featureFilePath}"))

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "one companion remains"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles('', false, [CucumberFeature.PASSWORD_RESET_V2]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        and: "the other is gone"
        expectedCompanionFiles('', false, [CucumberFeature.PRODUCT_SEARCH]).forEach {
            with(companionFile(it)) {
                !Files.exists(it)
            }
        }

        where:
        buildScriptLanguage << BuildScriptLanguage.values()
    }

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("build/generated-sources/cucumberCompanion-test/${companion.relativePath}")
    }

    void setupPlugin(BuildScriptLanguage language, Variant variant = Variant.IMPLICIT_WITH_TEST_SUITES) {
        switch (language) {
            case BuildScriptLanguage.GROOVY:
                switch (variant) {
                    case Variant.IMPLICIT:
                        setupPluginGroovy(false)
                        break
                    case Variant.IMPLICIT_WITH_TEST_SUITES:
                        setupPluginGroovy(true)
                        break
                    case Variant.IMPLICIT_RELAXED:
                        setupPluginGroovy(true, true)
                        break
                    case Variant.EXPLICIT:
                        setupPluginExplicitGroovy()
                        break
                }
                break
            case BuildScriptLanguage.KOTLIN:
                switch (variant) {
                    case Variant.IMPLICIT:
                        setupPluginKotlin(false)
                        break
                    case Variant.IMPLICIT_WITH_TEST_SUITES:
                        setupPluginKotlin(true)
                        break
                    case Variant.IMPLICIT_RELAXED:
                        setupPluginKotlin(true, true)
                        break
                    case Variant.EXPLICIT:
                        setupPluginExplicitKotlin()
                        break
                }
                break
            default:
                throw new IllegalArgumentException("Unsupported language: $language")
        }
    }

    enum BuildScriptLanguage {
        GROOVY, KOTLIN
    }

    enum Variant {
        IMPLICIT,
        IMPLICIT_WITH_TEST_SUITES,
        IMPLICIT_RELAXED,
        EXPLICIT;

        @Override
        String toString() {
            return name().replaceAll("_", " ")
        }
    }

    private void setupPluginGroovy(boolean withJvmTestSuite = true, boolean allowEmptySuites = false) {
        buildFile = workspace.file("build.gradle")
        settingsFile = workspace.file("settings.gradle")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                id('java')
                ${withJvmTestSuite ? "id('jvm-test-suite')" : ""}
                id('com.gradle.cucumber.companion')
            }
            repositories {
                mavenCentral()
            }
            ${allowEmptySuites ? """
            cucumberCompanion {
                allowEmptySuites = $allowEmptySuites
            }
            """.stripIndent(true) : ""}
            dependencies {
            ${dependenciesRequiredForExecution()}
            }
            tasks.withType(Test) {
                useJUnitPlatform()
                testLogging {
                    events("standardOut", "passed", "failed")
                }
            }
            """.stripIndent(true)
    }

    private void setupPluginKotlin(boolean withJvmTestSuite = true, boolean allowEmptySuites = false) {
        buildFile = workspace.file("build.gradle.kts")
        settingsFile = workspace.file("settings.gradle.kts")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                java
                ${withJvmTestSuite ? 'id("jvm-test-suite")' : ""}
                id("com.gradle.cucumber.companion")
            }
            repositories {
                mavenCentral()
            }
            ${allowEmptySuites ? """
            cucumberCompanion {
                allowEmptySuites.set($allowEmptySuites)
            }
            """.stripIndent(true) : ""}
            dependencies {
            ${dependenciesRequiredForExecution()}
            }
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    events("standardError", "skipped",  "standardOut", "passed", "failed")
                }
            }
            """.stripIndent(true)
    }

    private void setupPluginExplicitGroovy() {
        buildFile = workspace.file("build.gradle")
        settingsFile = workspace.file("settings.gradle")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                id('java')
                id('jvm-test-suite')
                id('com.gradle.cucumber.companion')
            }
            repositories {
                mavenCentral()
            }

            cucumberCompanion {
                enableForStandardTestTask = false
            }
            dependencies {
            ${dependenciesRequiredForExecution()}
            }
            testing {
                suites {
                    test {
                        useJUnitJupiter("$JUNIT_VERSION")
                        cucumberCompanion.generateCucumberSuiteCompanion(delegate)
                        targets {
                            all {
                                testTask.configure {
                                    testLogging {
                                        events("standardOut", "passed", "failed")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.stripIndent(true)
    }

    private void setupPluginExplicitKotlin() {
        buildFile = workspace.file("build.gradle.kts")
        settingsFile = workspace.file("settings.gradle.kts")
        settingsFile.text = ""
        buildFile.text = """\
            plugins {
                java
                `jvm-test-suite`
                id("com.gradle.cucumber.companion")
            }

            repositories {
                mavenCentral()
            }

            cucumberCompanion {
                enableForStandardTestTask.set(false)
            }
            dependencies {
            ${dependenciesRequiredForExecution()}
            }
            testing {
                suites {
                    val test by getting(JvmTestSuite::class) {
                        useJUnitJupiter("$JUNIT_VERSION")
                        generateCucumberSuiteCompanion(project)
                        targets {
                            all {
                                testTask.configure {
                                    testLogging {
                                        events("standardOut", "passed", "failed")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.stripIndent(true)
    }

    def dependenciesRequiredForExecution() {
        return """\
        testImplementation(platform("org.junit:junit-bom:$JUNIT_VERSION"))
        testImplementation("io.cucumber:cucumber-java:$CUCUMBER_VERSION")
        testImplementation("io.cucumber:cucumber-junit-platform-engine:$CUCUMBER_VERSION")
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.junit.platform:junit-platform-suite")
        testImplementation("org.junit.platform:junit-platform-launcher")
        """.stripIndent(true)
    }
}
