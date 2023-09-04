package org.gradle.cucumber.companion.maven

import groovy.xml.XmlSlurper
import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile

import java.nio.file.Files
import java.nio.file.Path

class GenerateCucumberCompanionMojoIntegrationTest extends BaseMavenFuncTest {

    @Delegate
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    def setup() {
        pom = workspace.file("pom.xml")
    }

    def "generate-cucumber-companion-files mojo generates valid companion files"() {
        given:
        createPom()
        createFeatureFiles(workspace)
        createStepFiles(workspace)

        when:
        def result = execute(distribution, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println it }

        and:
        def expectedCompanions = expectedCompanionFiles("Test")

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        distribution << distributions()
    }

    def "generate-cucumber-companion-files mojo generates valid companion files that are picked up by surefire"() {
        given:
        createPom()
        createFeatureFiles(workspace)
        createStepFiles(workspace)

        when:
        def result = execute(distribution, "test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each { println it }

        and:
        def expectedCompanions = expectedCompanionFiles("Test")
        expectedCompanions.forEach {
            verifyAll(testReport(it)) {
                Files.exists(it)
                def testsuite = new XmlSlurper().parse(it)
                testsuite.testcase.size() == 1
            }
        }

        where:
        distribution << distributions()
    }

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("target/generated-test-sources/cucumberCompanion/${companion.relativePath}")
    }

    Path testReport(ExpectedCompanionFile companion) {
        workspace.resolve("target/surefire-reports/TEST-${companion.packageName ? companion.packageName + '.' : ''}${companion.className}.xml")
    }

    private void createPom() {
        pom.text = """\
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <groupId>tmp</groupId>
        <artifactId>project-to-test</artifactId>
        <version>1.0-SNAPSHOT</version>
        <packaging>jar</packaging>

        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            <project.build.reportEncoding>UTF-8</project.build.reportEncoding>
            <maven.compiler.source>1.8</maven.compiler.source>
            <maven.compiler.target>1.8</maven.compiler.target>
        </properties>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.gradle.cucumber.companion</groupId>
                    <artifactId>cucumber-companion-plugin</artifactId>
                    <version>\${it-project.version}</version>
                    <executions>
                        <execution>
                            <id>generate-companion</id>
                            <goals>
                                <goal>generate-cucumber-companion-files</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>

                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-clean-plugin</artifactId>
                    <version>3.3.1</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-resources-plugin</artifactId>
                    <version>3.3.1</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.1.2</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <version>3.1.2</version>
                </plugin>
            </plugins>
        </build>
        <dependencyManagement>
          <dependencies>
                <dependency>
                    <groupId>org.junit</groupId>
                    <artifactId>junit-bom</artifactId>
                    <version>5.10.0</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.junit.platform</groupId>
                <artifactId>junit-platform-suite</artifactId>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>io.cucumber</groupId>
                <artifactId>cucumber-java</artifactId>
                <version>7.12.1</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>io.cucumber</groupId>
                <artifactId>cucumber-junit-platform-engine</artifactId>
                <version>7.12.1</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </project>""".stripIndent(true)
    }
}
