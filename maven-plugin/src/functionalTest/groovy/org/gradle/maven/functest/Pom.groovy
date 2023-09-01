package org.gradle.maven.functest

class Pom {

    def plugins = [] as List<ArtifactCoordinates>
    def dependencies = [] as List<ArtifactCoordinates>
    def dependencyManagement = [] as List<ArtifactCoordinates>
    def properties = [:] as Map<String, String>

    def property(String property, String value) {
        properties.put(property, value)
    }

    def plugin(String groupId, String artifactId, String version, Closure<?> extra = { "" }) {
        plugins.add(new ArtifactCoordinates(groupId, artifactId, version, null, null, extra))
    }

    def dependency(String groupId, String artifactId, String version = null, String scope = "compile") {
        dependencies.add(new ArtifactCoordinates(groupId, artifactId, version, scope, null, { "" }))
    }

    def managedDependency(String groupId, String artifactId, String version = null, String scope = "compile", String type = null) {
        dependencyManagement.add(new ArtifactCoordinates(groupId, artifactId, version, scope, type, { "" }))
    }

    class ArtifactCoordinates {
        String groupId
        String artifactId
        String version
        String scope
        String type
        Closure<?> extra

        ArtifactCoordinates(String groupId, String artifactId, String version, String scope, String type, Closure<?> extra) {
            this.groupId = groupId
            this.artifactId = artifactId
            this.version = version
            this.scope = scope
            this.type = type
            this.extra = extra
        }

        def asDependency() {
            return "<dependency>${toString()}</dependency>"
        }

        def asPlugin() {
            return "<plugin>${toString()}</plugin>"
        }

        @Override
        String toString() {
            return "<groupId>${groupId}</groupId><artifactId>${artifactId}</artifactId>${version != null ? "<version>${version}</version>" : ""}${scope != null ? "<scope>${scope}</scope>" : ""}${type != null ? "<type>${type}</type>" : ""}${extra.call()}"
        }
    }

    @Override
    String toString() {
        """
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>

            <groupId>tmp</groupId>
            <artifactId>project-to-test</artifactId>
            <version>1.0-SNAPSHOT</version>
            <packaging>jar</packaging>

            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <project.build.reportEncoding>UTF-8</project.build.reportEncoding>
${properties.empty ? '' :
    "${properties.entrySet().collect { indent(4) + "<${it.key}>${it.value}</${it.key}>" }.join(System.lineSeparator())}"
            }
            </properties>

            <build>
                <plugins>
${plugins.collect { indent(6) + it.asPlugin() }.join(System.lineSeparator())}
                </plugins>
            </build>
            ${dependencyManagement.empty ? "" : """
            <dependencyManagement>
                <dependencies>
${dependencyManagement.collect { indent(5) + it.asDependency() }.join(System.lineSeparator())}
                </dependencies>
            </dependencyManagement>
            """}
            ${dependencies.empty ? "" : """
            <dependencies>
${dependencies.collect { indent(4) + it.asDependency() }.join(System.lineSeparator())}
            </dependencies>
            """}
        </project>
        """.stripIndent(true)
    }

    private String indent(int n) {
        return " " * n
    }
}
