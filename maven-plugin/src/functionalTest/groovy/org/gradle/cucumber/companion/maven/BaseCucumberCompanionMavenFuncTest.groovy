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

import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import org.gradle.cucumber.companion.testcontext.TestContext
import org.gradle.maven.functest.BaseMavenFuncTest
import org.gradle.maven.functest.MavenDistribution
import org.gradle.maven.functest.Pom

import java.nio.file.Path

abstract class BaseCucumberCompanionMavenFuncTest extends BaseMavenFuncTest {

    static final String JUNIT_VERSION = TestContext.getRequiredValue("junitVersion")
    static final String CUCUMBER_VERSION = TestContext.getRequiredValue("cucumberVersion")
    static final String SUREFIRE_VERSION = TestContext.getRequiredValue("surefireVersion")
    static final String FAILSAFE_VERSION = TestContext.getRequiredValue("failsafeVersion")

    MavenDistribution maven = MavenDistribution.theSingleMavenDistribution()
    @Delegate
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.fileSystem.resolve("target/generated-test-sources/cucumberCompanion/${companion.relativePath}")
    }

    Path sureFireTestReport(ExpectedCompanionFile companion) {
        workspace.fileSystem.resolve("target/surefire-reports/TEST-${companion.packageName ? companion.packageName + '.' : ''}${companion.className}.xml")
    }

    Path failsafeFireTestReport(ExpectedCompanionFile companion) {
        workspace.fileSystem.resolve("target/failsafe-reports/TEST-${companion.packageName ? companion.packageName + '.' : ''}${companion.className}.xml")
    }

    def createProject(@DelegatesTo(value = Pom.class, strategy = Closure.DELEGATE_FIRST) Closure<?> pom = {}) {
        workspace.pom {
            addProperty("maven.compiler.source", "1.8")
            addProperty("maven.compiler.target", "1.8")
            addPlugin("org.apache.maven.plugins", "maven-clean-plugin", "3.3.1")
            addPlugin("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0")
            addPlugin("org.apache.maven.plugins", "maven-resources-plugin", "3.3.1")
            addPlugin("org.apache.maven.plugins", "maven-surefire-plugin", SUREFIRE_VERSION)
            addPlugin("org.apache.maven.plugins", "maven-failsafe-plugin", FAILSAFE_VERSION) {
                executions {
                    execution {
                        goals {
                            goal("integration-test")
                            goal("verify")
                        }
                    }
                }

            }
            addPlugin("org.gradle.cucumber.companion", "cucumber-companion-maven-plugin", '${it-project.version}') {
                executions {
                    execution {
                        goals {
                            goal("generate-cucumber-companion-files")
                        }
                    }
                }
            }
            addManagedDependency("org.junit", "junit-bom", JUNIT_VERSION)
            addDependency("org.junit.jupiter", "junit-jupiter", null, "test")
            addDependency("org.junit.platform", "junit-platform-suite", null, "test")
            addDependency("io.cucumber", "cucumber-java", CUCUMBER_VERSION, "test")
            addDependency("io.cucumber", "cucumber-junit-platform-engine", CUCUMBER_VERSION, "test")
            it.with(pom)
        }
    }
}
