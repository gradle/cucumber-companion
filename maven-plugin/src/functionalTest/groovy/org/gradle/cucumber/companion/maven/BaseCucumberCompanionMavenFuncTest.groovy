package org.gradle.cucumber.companion.maven

import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import org.gradle.maven.functest.BaseMavenFuncTest

import java.nio.file.Path

class BaseCucumberCompanionMavenFuncTest extends BaseMavenFuncTest {

    static final String JUNIT_VERSION = "5.10.0"
    static final String CUCUMBER_VERSION = "7.12.1"
    static final String SUREFIRE_VERSION = "3.1.2"

    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("target/generated-test-sources/cucumberCompanion/${companion.relativePath}")
    }

    Path testReport(ExpectedCompanionFile companion) {
        workspace.resolve("target/surefire-reports/TEST-${companion.packageName ? companion.packageName + '.' : ''}${companion.className}.xml")
    }

    def createProject(
        String junit5Version = JUNIT_VERSION,
        String cucumberVersion = CUCUMBER_VERSION,
        String surefireVersion = SUREFIRE_VERSION) {

        workspace.pom {
            it.property("maven.compiler.source", "1.8")
            it.property("maven.compiler.target", "1.8")
            it.plugin("org.apache.maven.plugins", "maven-clean-plugin", "3.3.1")
            it.plugin("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0")
            it.plugin("org.apache.maven.plugins", "maven-resources-plugin", "3.3.1")
            it.plugin("org.apache.maven.plugins", "maven-surefire-plugin", surefireVersion)
            it.plugin("org.gradle.cucumber.companion", "cucumber-companion-plugin", '${it-project.version}') {
                '''
                    <executions>
                        <execution>
                            <id>generate-companion</id>
                            <goals>
                                <goal>generate-cucumber-companion-files</goal>
                            </goals>
                        </execution>
                    </executions>
                '''
            }
            it.managedDependency("org.junit", "junit-bom", junit5Version, "import", "pom")
            it.dependency("org.junit.jupiter", "junit-jupiter", null, "test")
            it.dependency("org.junit.platform", "junit-platform-suite", null, "test")
            it.dependency("io.cucumber", "cucumber-java", cucumberVersion, "test")
            it.dependency("io.cucumber", "cucumber-junit-platform-engine", cucumberVersion, "test")
        }
    }
}
