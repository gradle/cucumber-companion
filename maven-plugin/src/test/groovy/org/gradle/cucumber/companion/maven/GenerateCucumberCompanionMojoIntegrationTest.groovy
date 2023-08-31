package org.gradle.cucumber.companion.maven

import io.takari.maven.testing.executor.MavenRuntime
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Files
import java.nio.file.Path

class GenerateCucumberCompanionMojoIntegrationTest extends Specification {

    @TempDir
    FileSystemFixture workspace
    Path pom

    def setup() {
        pom = workspace.file("pom.xml")
    }

    def "generate-cucumber-companion-files mojo generates valid companion file" () {
        given:
        createPom()
        createFeatureFiles()
        def forkedRunner = MavenRuntime.forkedBuilder(new File(System.getProperty("testInternal.mavenInstallDir"))).build()

        when:
        def result = forkedRunner.forProject(workspace.currentPath.toFile()).execute("test", "-X")

        then:
        noExceptionThrown()
        result.assertErrorFreeLog()
        result.log.each {println it}

        and:
        def expectedCompanions = [
            ['Product Search', ''],
            ['Shopping Cart', ''],
            ['User Registration', 'user/'],
            ['Password Reset', 'user/'],
            ['User Profile', 'user/']
        ]

        expectedCompanions.forEach {
            def companionFile =  companionFile(*it)
            def (name, path) = it
            def pkg = path == '' ? null : path.dropRight(1).replaceAll('/', '.')
            verifyAll {
                Files.exists(companionFile)
                def expected = """${pkg ? "                    package $pkg;\n\n" : ''}\
                    @org.junit.platform.suite.api.Suite
                    @org.junit.platform.suite.api.SelectClasspathResource("${path}${name}.feature")
                    class ${safeName(name)} {}
                    """.stripIndent(true)
                companionFile.text == expected
            }
        }
    }

    private String safeName(String name) {
        name.replaceAll(" ", "_")
    }

    Path companionFile(String name, String path = "") {
        return workspace.resolve("target/generated-test-sources/cucumberCompanion/${path}${safeName(name)}Test.java")
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
                    <version>${System.getProperty("testInternal.pluginVersion")}</version>
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


    private void createFeatureFiles() {
        workspace.create {
            dir("src/test/resources") {
                file("Product Search.feature") << """\
                    Feature: Product Search
                      Scenario: Users can search for products
                        Given a user is on the homepage
                        When they enter a product name in the search bar
                        And click the "Search" button
                        Then they should see a list of matching products
                """.stripIndent(true)
                file("Shopping Cart.feature") << """\
                    Feature: Shopping Cart
                      Scenario: Users can add and remove items from the shopping cart
                        Given a user has added items to their cart
                        When they remove an item from the cart
                        Then the item should be removed from the cart
                        And the cart total should be updated accordingly
                """.stripIndent(true)
                dir("user") {
                    file("User Registration.feature") << """\
                        Feature: User Registration
                          Scenario: New users can create an account
                            Given a user is on the registration page
                            When they fill in their information
                            And click the "Sign Up" button
                            Then they should be registered and logged in
                    """.stripIndent(true)
                    file("Password Reset.feature") << """\
                        Feature: Password Reset
                          Scenario: Users can reset their password
                            Given a user is on the password reset page
                            When they enter their email address
                            And click the "Reset Password" button
                            Then they should receive an email with instructions to reset their password
                    """.stripIndent(true)
                    file("User Profile.feature") << """\
                        Feature: User Profile
                          Scenario: Users can update their profile information
                            Given a user is logged in
                            When they navigate to their profile page
                            And edit their profile information
                            And click the "Save" button
                            Then their profile information should be updated
                    """.stripIndent(true)
                }
            }
        }
    }
}
