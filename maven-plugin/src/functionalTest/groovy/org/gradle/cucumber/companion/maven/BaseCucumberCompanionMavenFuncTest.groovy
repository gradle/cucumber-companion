package org.gradle.cucumber.companion.maven

import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import org.gradle.maven.functest.BaseMavenFuncTest
import org.gradle.maven.functest.MavenDistribution
import org.gradle.maven.functest.Pom

import java.nio.file.Path

class BaseCucumberCompanionMavenFuncTest extends BaseMavenFuncTest {

    static final String JUNIT_VERSION = "5.10.0"
    static final String CUCUMBER_VERSION = "7.12.1"
    static final String SUREFIRE_VERSION = "3.1.2"

    MavenDistribution maven = MavenDistribution.theSingleMavenDistribution()
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.fileSystem.resolve("target/generated-test-sources/cucumberCompanion/${companion.relativePath}")
    }

    Path testReport(ExpectedCompanionFile companion) {
        workspace.fileSystem.resolve("target/surefire-reports/TEST-${companion.packageName ? companion.packageName + '.' : ''}${companion.className}.xml")
    }

    def createProject(@DelegatesTo(value = Pom.class, strategy = Closure.DELEGATE_FIRST) Closure<?> pom = {}) {
        workspace.pom {
            addProperty("maven.compiler.source", "1.8")
            addProperty("maven.compiler.target", "1.8")
            addPlugin("org.apache.maven.plugins", "maven-clean-plugin", "3.3.1")
            addPlugin("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0")
            addPlugin("org.apache.maven.plugins", "maven-resources-plugin", "3.3.1")
            addPlugin("org.apache.maven.plugins", "maven-surefire-plugin", SUREFIRE_VERSION)
            addPlugin("org.gradle.cucumber.companion", "cucumber-companion-plugin", '${it-project.version}') {
                executions {
                    execution {
                        id("generate-companion")
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
