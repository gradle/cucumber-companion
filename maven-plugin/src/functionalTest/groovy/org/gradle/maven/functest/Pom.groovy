package org.gradle.maven.functest


import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil

class Pom {

    private static final String BASE_POM_XML =
        // language=XML
        """\
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>

            <groupId>tmp</groupId>
            <artifactId>project-to-test</artifactId>
            <version>1.0-SNAPSHOT</version>
            <packaging>jar</packaging>
        </project>
    """

    def plugins = [] as LinkedHashSet<ArtifactCoordinates>
    def dependencies = [] as LinkedHashSet<ArtifactCoordinates>
    def dependencyManagement = [] as LinkedHashSet<ArtifactCoordinates>
    def properties = [:] as LinkedHashMap<String, String>

    Pom() {
        properties.putAll([
            "project.build.sourceEncoding": "UTF-8",
            "project.build.reportEncoding": "UTF-8"
        ])
    }

    def property(String property, String value) {
        properties.put(property, value)
    }

    def plugin(String groupId, String artifactId, String version, Closure<?> extra = {}) {
        plugins.add(new ArtifactCoordinates(groupId, artifactId, version, null, null, extra))
    }

    def dependencyWithManagedVersion(String groupId, String artifactId, String scope = "compile") {
        dependencies.add(new ArtifactCoordinates(groupId, artifactId, null, scope, null, {}))
    }

    def dependency(String groupId, String artifactId, String version = null, String scope = "compile") {
        dependencies.add(new ArtifactCoordinates(groupId, artifactId, version, scope, null, {}))
    }

    def dependencyManagement(String groupId, String artifactId, String version = null, String scope = "compile", String type = null) {
        dependencyManagement.add(new ArtifactCoordinates(groupId, artifactId, version, scope, type, {}))
    }

    @Override
    String toString() {
        def pomXml = new XmlSlurper(false, false).parseText(BASE_POM_XML)
        pomXml.appendNode {
            if (!this.properties.empty) {
                properties {
                    this.properties.collect { k, v -> "$k" { mkp.yield(v) } }
                }
            }

            if (!this.dependencyManagement.empty) {
                dependencyManagement {
                    dependencies { deps ->
                        this.dependencyManagement.each {
                            deps.dependency { dep ->
                                getOwner().with(it.markup())
                            }
                        }
                    }
                }
            }

            if (!this.dependencies.empty) {
                dependencies { deps ->
                    this.dependencies.each {
                        deps.dependency { dep ->
                            getOwner().with(it.markup())
                        }
                    }
                }
            }

            if (!this.plugins.empty) {
                build {
                    plugins { plugs ->
                        this.plugins.each {
                            plugs.plugin { plug ->
                                getOwner().with(it.markup())
                            }
                        }
                    }
                }
            }
        }
        return XmlUtil.serialize(pomXml)
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

        def markup() {
            return {
                groupId(this.groupId)
                artifactId(this.artifactId)
                this.version?.tap { version(it) }
                this.scope?.tap { scope(it) }
                this.type?.tap { type(it) }
            }.andThen(this.extra)
        }

        boolean equals(obj) {
            return obj == this || obj instanceof ArtifactCoordinates
                && groupId == (obj as ArtifactCoordinates).groupId
                && artifactId == (obj as ArtifactCoordinates).artifactId
        }

        int hashCode() {
            return Objects.hash(groupId, artifactId)
        }
    }

}
