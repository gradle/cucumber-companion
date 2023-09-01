package org.gradle.cucumber.companion.maven

import io.takari.maven.testing.executor.MavenRuntime
import org.gradle.cucumber.companion.fixtures.CompanionAssertions
import org.gradle.cucumber.companion.fixtures.CucumberFixture
import org.gradle.cucumber.companion.fixtures.ExpectedCompanionFile
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class GenerateCucumberCompanionMojoIntegrationTest extends Specification {


    @TempDir
    FileSystemFixture workspace
    Path pom

    @Delegate
    CucumberFixture cucumberFixture = new CucumberFixture()
    CompanionAssertions companionAssertions = new CompanionAssertions(this::companionFile)

    def setup() {
        pom = workspace.file("pom.xml")
    }

    private List<String> mavenHomes() {
        return System.getProperties().entrySet().stream()
            .filter { it.key.toString().startsWith("testContext.internal.mavenHome.") }
            .map { it.value }
            .collect(Collectors.toList())
    }

    def "generate-cucumber-companion-files mojo generates valid companion files" () {
        given:
        createPom()
        createFeatureFiles(workspace)
        createStepFiles(workspace)
        def forkedRunner = MavenRuntime.forkedBuilder(new File(mavenHome)).build()

        when:
        def result = forkedRunner.forProject(workspace.currentPath.toFile()).execute("test-compile")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each {println it}

        and:
        def expectedCompanions = expectedCompanionFiles("Test")

        expectedCompanions.forEach {
            companionAssertions.assertCompanionFile(it)
        }

        where:
        mavenHome << mavenHomes()
    }

    def "generate-cucumber-companion-files mojo generates valid companion files that are picked up by surefire" () {
        given:
        createPom()
        createFeatureFiles(workspace)
        createStepFiles(workspace)
        def forkedRunner = MavenRuntime.forkedBuilder(new File(mavenHome)).build()

        when:
        def result = forkedRunner.forProject(workspace.currentPath.toFile()).execute("test")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each {println it}

        and:
        workspace.resolve("target/surefires-reports")

        where:
        mavenHome << mavenHomes()
    }

    private String safeName(String name) {
        name.replaceAll(" ", "_")
    }

    Path companionFile(ExpectedCompanionFile companion) {
        return workspace.resolve("target/generated-test-sources/cucumberCompanion/${companion.relativePath}")
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