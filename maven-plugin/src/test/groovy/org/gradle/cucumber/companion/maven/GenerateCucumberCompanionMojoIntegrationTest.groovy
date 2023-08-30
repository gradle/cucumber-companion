package org.gradle.cucumber.companion.maven

import io.takari.maven.testing.executor.MavenRuntime
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Path

class GenerateCucumberCompanionMojoIntegrationTest extends Specification {

    @TempDir
    FileSystemFixture workspace
    Path rootPom
    Path testResources

    def setup() {
        rootPom = workspace.file("pom.xml")
        workspace.create {
            dir("src") {
                dir("test") {
                    testResources = dir("resources")
                }
            }
        }
    }

    def "hello world" () {
        given:
        rootPom.text = """\
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <groupId>tmp</groupId>
        <artifactId>project-to-test</artifactId>
        <version>1.0-SNAPSHOT</version>
        <packaging>pom</packaging>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.gradle.cucumber.companion</groupId>
                    <artifactId>cucumber-companion-plugin</artifactId>
                    <version>1.0.0</version>
                </plugin>
            </plugins>
        </build>
    </project>""".stripIndent(true)


        def forkedRunner = MavenRuntime.forkedBuilder(new File(System.getProperty("testInternal.mavenInstallDir"))).build()

        when:
        def result = forkedRunner.forProject(workspace.currentPath.toFile()).execute("help:effective-pom")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each {println it}
    }
}
