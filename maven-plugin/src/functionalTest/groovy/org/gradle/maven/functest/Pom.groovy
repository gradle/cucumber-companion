package org.gradle.maven.functest

import groovy.transform.Memoized

class Pom {

    def plugins = [] as LinkedHashSet<ArtifactCoordinates>
    def dependencies = [] as LinkedHashSet<ArtifactCoordinates>
    def dependencyManagement = [] as LinkedHashSet<ArtifactCoordinates>
    def properties = [:] as LinkedHashMap<String, String>

    def property(String property, String value) {
        properties.put(property, value)
    }

    def plugin(String groupId, String artifactId, String version, Closure<?> extra = { "" }) {
        plugins.add(new ArtifactCoordinates(groupId, artifactId, version, null, null, extra))
    }

    def dependencyWithManagedVersion(String groupId, String artifactId, String scope = "compile") {
        dependencies.add(new ArtifactCoordinates(groupId, artifactId, null, scope, null, { "" }))
    }

    def dependency(String groupId, String artifactId, String version = null, String scope = "compile") {
        dependencies.add(new ArtifactCoordinates(groupId, artifactId, version, scope, null, { "" }))
    }

    def dependencyManagement(String groupId, String artifactId, String version = null, String scope = "compile", String type = null) {
        dependencyManagement.add(new ArtifactCoordinates(groupId, artifactId, version, scope, type, { "" }))
    }

    final class ArtifactCoordinates {
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

        boolean equals(o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.class) return false

            ArtifactCoordinates that = (ArtifactCoordinates) o

            if (artifactId != that.artifactId) return false
            if (groupId != that.groupId) return false

            return true
        }

        int hashCode() {
            int result
            result = (groupId != null ? groupId.hashCode() : 0)
            result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0)
            return result
        }

        @Override
        String toString() {
            return "<groupId>${groupId}</groupId><artifactId>${artifactId}</artifactId>${version != null ? "<version>${version}</version>" : ""}${scope != null ? "<scope>${scope}</scope>" : ""}${type != null ? "<type>${type}</type>" : ""}${extra.call()}"
        }
    }

    @Override
    String toString() {
        """\
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
            "${properties.entrySet().collect { indent(4) + "<${it.key}>${it.value}</${it.key}>" }.join("\n")}"
        }
            </properties>

            <build>
                <plugins>
${plugins.collect { indent(6) + it.asPlugin() }.join("\n")}
                </plugins>
            </build>
            ${dependencyManagement.empty ? "" : """\
            <dependencyManagement>
                <dependencies>
${dependencyManagement.collect { indent(5) + it.asDependency() }.join("\n")}
                </dependencies>
            </dependencyManagement>
            """}
            ${dependencies.empty ? "" : """\
            <dependencies>
${dependencies.collect { indent(4) + it.asDependency() }.join("\n")}
            </dependencies>
            """}
        </project>
        """.stripIndent(true)
    }

    @Memoized
    private String indent(int n) {
        return " " * (n * 4)
    }
}
