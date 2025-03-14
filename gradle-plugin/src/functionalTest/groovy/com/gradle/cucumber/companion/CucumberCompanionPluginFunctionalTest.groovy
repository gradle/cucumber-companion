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
package com.gradle.cucumber.companion

import com.gradle.cucumber.companion.fixtures.CompanionAssertions
import com.gradle.cucumber.companion.fixtures.CucumberFeature
import com.gradle.cucumber.companion.fixtures.CucumberFixture
import com.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import com.gradle.cucumber.companion.testcontext.TestContext
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Files
import java.nio.file.Path

class CucumberCompanionPluginFunctionalTest extends Specification {

    static final String CUCUMBER_VERSION = TestContext.getRequiredValue("cucumberVersion")
    static final String JUNIT_VERSION = TestContext.getRequiredValue("junitVersion")
    static final GradleVersion GRADLE_VERSION = GradleVersion.version(TestContext.getRequiredValue("gradleVersion"))
    static final GradleVersion GRADLE_7_3 = GradleVersion.version("7.3")

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
        // older versions of Gradle do not treat FileCollections with empty directories as empty
        def expectedOutcome = GRADLE_VERSION <= GRADLE_7_3 ? TaskOutcome.SUCCESS : TaskOutcome.NO_SOURCE
        result.task(":testGenerateCucumberSuiteCompanion").outcome == expectedOutcome

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

        def expectedCompanions = expectedCompanionFiles()

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), Variant.values()].combinations()
    }

    def "can customize classes to allow for empty suites"(BuildScriptLanguage buildScriptLanguage, Variant variant) {
        given:
        setupPlugin(buildScriptLanguage, variant, 'allowEmptySuites.set(true)')
        createFeatureFiles(workspace)

        when:
        def result = run("testGenerateCucumberSuiteCompanion")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        def expectedCompanions = expectedCompanionFiles(allowEmptySuites: true)

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        [buildScriptLanguage, variant] << [BuildScriptLanguage.values(), Variant.values()].combinations()
    }

    def "can customize generated classes"(BuildScriptLanguage buildScriptLanguage, Variant variant, String baseClass, List<String> interfaces, List<String> annotations) {
        given:
        setupPlugin(buildScriptLanguage, variant, """
            customizeGeneratedClasses {
                ${baseClass ? "baseClass.set(\"$baseClass\")" : ''}
                ${interfaces ? "${interfaces.collect { "interfaces.add(\"$it\")" }.join('\n')}" : ''}
                ${annotations ? "${annotations.collect { "annotations.add(\"${it.replace(/"/, /\"/)}\")" }.join('\n')}" : ''}
            }
            """.stripIndent(true))
        createFeatureFiles(workspace)

        and:
        if (baseClass) {
            createBaseClass(workspace, baseClass)
        }
        interfaces.each {
            createInterface(workspace, it)
        }

        when:
        def result = run("testGenerateCucumberSuiteCompanion")

        then:
        result.output.contains("testGenerateCucumberSuiteCompanion")

        def expectedCompanions = expectedCompanionFiles(baseClass: baseClass, interfaces: interfaces, annotations: annotations)

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        [buildScriptLanguage, variant, [baseClass, interfaces, annotations]] << [BuildScriptLanguage.values(), Variant.values(),
            [
                [null, [], []],
                ["base.Base", [], []],
                [null, ["base.IFace"], []],
                [null, ["base.IFace", "base.IOther"], []],
                [null, [], ['@org.junit.jupiter.api.Tag("tag")']],
                [null, [], ['@org.junit.jupiter.api.Tag("tag1")', '@org.junit.jupiter.api.Tag("tag2")']],
                ["base.Base", ["base.IFace"], ['@org.junit.jupiter.api.Tag("tag")']]
            ]
        ].combinations()

    }

    def "generated companion files are picked up by Gradle's test task and tests succeed"(BuildScriptLanguage buildScriptLanguage, Variant variant) {
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

    def "generated companion files are picked up by Gradle's test task and only discovered tests succeed"(BuildScriptLanguage buildScriptLanguage, Variant variant) {
        given:
        def allSucceedingFeatures = CucumberFeature.allSucceeding()
        def discoveredSucceedingFeatures = [CucumberFeature.USER_PROFILE]
        setupPlugin(buildScriptLanguage, variant, 'allowEmptySuites.set(true)')
        createFeatureFiles(workspace, allSucceedingFeatures)
        createStepFiles(workspace, allSucceedingFeatures)
        createPostDiscoveryFilter(workspace, "$CucumberFeature.USER_PROFILE.packageName.$CucumberFeature.USER_PROFILE.className")
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
        [buildScriptLanguage, variant] << [
            BuildScriptLanguage.values(),
            Variant.values()
        ].combinations()
    }

    def "can run failing cucumber test"(BuildScriptLanguage buildScriptLanguage, Variant variant) {
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

        expectedCompanionFiles([CucumberFeature.PRODUCT_SEARCH]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "adding another feature"
        createFeatureFiles(workspace, [CucumberFeature.PASSWORD_RESET])

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "both companion files are present"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles([CucumberFeature.PRODUCT_SEARCH, CucumberFeature.PASSWORD_RESET]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "modifying an existing feature"
        createFeatureFiles(workspace, [CucumberFeature.PASSWORD_RESET_V2])

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "both companion files are present"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles([CucumberFeature.PRODUCT_SEARCH, CucumberFeature.PASSWORD_RESET_V2]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        when: "deleting a feature file"
        Files.delete(workspace.file("src/test/resources/${CucumberFeature.PRODUCT_SEARCH.featureFilePath}"))

        and: "running the generate task again"
        result = run("testGenerateCucumberSuiteCompanion")

        then: "one companion remains"
        result.output.contains("testGenerateCucumberSuiteCompanion")

        expectedCompanionFiles([CucumberFeature.PASSWORD_RESET_V2]).forEach {
            companionAssertions.assertCompanionFile(it)
        }

        and: "the other is gone"
        expectedCompanionFiles([CucumberFeature.PRODUCT_SEARCH]).forEach {
            with(companionFile(it)) {
                !Files.exists(it)
            }
        }

        where:
        buildScriptLanguage << BuildScriptLanguage.values()
    }

    def "supports multiple resource directories"(BuildScriptLanguage buildScriptLanguage) {
        given:
        def succeedingFeatures = CucumberFeature.allSucceeding()

        and:
        def featureSets = succeedingFeatures.collate(2)
        createFeatureFiles(workspace, featureSets[0])
        createStepFiles(workspace, succeedingFeatures)

        and:
        def additionalConfig = ""
        featureSets[1..-1].eachWithIndex { features, idx ->
            def srcDirectory = "src/test/features$idx"
            workspace.dir(srcDirectory) {
                features.forEach {
                    file(it.featureFilePath).text = it.featureFileContent
                }
            }
            additionalConfig += "srcDir(\"$srcDirectory\")\n"
        }

        additionalConfig = """
        sourceSets {
            named("test") {
                resources {
                    ${additionalConfig}
                }
            }
        }"""

        setupPlugin(buildScriptLanguage, Variant.IMPLICIT_WITH_TEST_SUITES, additionalConfig)

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
        buildScriptLanguage << BuildScriptLanguage.values()
    }

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("build/generated-sources/cucumberCompanion-test/${companion.relativePath}")
    }

    def setupPlugin(BuildScriptLanguage language, Variant variant = Variant.IMPLICIT_WITH_TEST_SUITES, String additionalConfig = "") {
        switch (language) {
            case BuildScriptLanguage.GROOVY:
                switch (variant) {
                    case Variant.IMPLICIT:
                        setupPluginGroovy(false, additionalConfig)
                        break
                    case Variant.IMPLICIT_WITH_TEST_SUITES:
                        setupPluginGroovy(true, additionalConfig)
                        break
                    case Variant.EXPLICIT:
                        setupPluginExplicitGroovy(additionalConfig)
                        break
                }
                break
            case BuildScriptLanguage.KOTLIN:
                switch (variant) {
                    case Variant.IMPLICIT:
                        setupPluginKotlin(false, additionalConfig)
                        break
                    case Variant.IMPLICIT_WITH_TEST_SUITES:
                        setupPluginKotlin(true, additionalConfig)
                        break
                    case Variant.EXPLICIT:
                        setupPluginExplicitKotlin(additionalConfig)
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
        EXPLICIT;

        @Override
        String toString() {
            return name().replaceAll("_", " ")
        }
    }

    private void setupPluginGroovy(boolean withJvmTestSuite = true, String additionalConfig = "") {
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
            ${pluginBlock(additionalConfig)}
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

    private void setupPluginKotlin(boolean withJvmTestSuite = true, String additionalConfig = "") {
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
            ${pluginBlock(additionalConfig)}
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

    private void setupPluginExplicitGroovy(String additionalTaskConfig = "") {
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
                        cucumberCompanion.generateCucumberSuiteCompanion(delegate) ${block(additionalTaskConfig)}
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

    private void setupPluginExplicitKotlin(String additionalTaskConfig = "") {
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
                        generateCucumberSuiteCompanion(project) ${block(additionalTaskConfig)}
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

    private static String dependenciesRequiredForExecution() {
        return """\
        testImplementation(platform("org.junit:junit-bom:$JUNIT_VERSION"))
        testImplementation("io.cucumber:cucumber-java:$CUCUMBER_VERSION")
        testImplementation("io.cucumber:cucumber-junit-platform-engine:$CUCUMBER_VERSION")
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.junit.platform:junit-platform-suite")
        testImplementation("org.junit.platform:junit-platform-launcher")
        """.stripIndent(true)
    }

    private static String block(String block) {
        return block?.trim() ? "{\n${block}\n}" : ""
    }

    private static String pluginBlock(String block) {
        return block?.trim() ? "cucumberCompanion {\n${block}\n}" : ""
    }
}
