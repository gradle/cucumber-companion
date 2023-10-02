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
package org.gradle.maven.functest

import groovy.namespace.QName
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import groovy.xml.XmlParser

import java.nio.charset.Charset

@CompileStatic
final class Pom extends XmlTree {

    public static final String DEFAULT_GROUP_ID = 'com.gradle'
    public static final String DEFAULT_VERSION = '0.0'

    private static final QName GROUP_ID_QNAME = new QName('groupId')
    private static final QName ARTIFACT_ID_QNAME = new QName('artifactId')
    private static final QName VERSION_QNAME = new QName('version')
    private static final QName SCOPE_QNAME = new QName('scope')
    private static final QName TYPE_QNAME = new QName('type')

    private static String initialPom(String artifactId, String version, String packaging) {
        """\
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>${DEFAULT_GROUP_ID}</groupId>
                <artifactId>${artifactId}</artifactId>
                ${packaging ? "<packaging>$packaging</packaging>" : ''}
                <version>${version}</version>
                <properties>
                    <project.build.sourceEncoding>${Charset.defaultCharset().name()}</project.build.sourceEncoding>
                    <project.build.reportEncoding>${Charset.defaultCharset().name()}</project.build.reportEncoding>
                </properties>
            </project>
        """.stripIndent(true)
    }

    Pom(String artifactId, String version = DEFAULT_VERSION, String packaging = 'jar') {
        super(new XmlParser().parseText(initialPom(artifactId, version, packaging)))
    }

    Pom addProperty(String name, String value = null) {
        addXmlAt('properties', value != null ? "<$name>$value</$name>" : "<$name/>")
        return this
    }

    Pom addPlugin(String groupId, String artifactId, String version = null, @DelegatesTo(MarkupBuilder) Closure closure) {
        addPlugin(groupId, artifactId, version, runOnMarkupBuilder(closure))
    }

    Pom addPlugin(String groupId, String artifactId, String version = null, String config = "") {
        Node plugin = findPlugin(groupId, artifactId)
        if (plugin) {
            if (version) {
                assert plugin[VERSION_QNAME].text() == version
            }
            if (config) {
                mergeXmlAt(plugin, config)
            }
        } else {
            addXmlAt('build/plugins', """\
                <plugin>
                    <groupId>$groupId</groupId>
                    <artifactId>$artifactId</artifactId>
                    ${version ? "<version>$version</version>" : ""}
                    $config
                </plugin>
            """.stripIndent(true))
        }
        return this
    }

    Pom removePlugin(String groupId, String artifactId) {
        Node plugin = findPlugin(groupId, artifactId)
        if (plugin) {
            plugin.parent().remove(plugin)
        }
        return this
    }

    Pom replacePlugin(String groupId, String artifactId, String version = null, @DelegatesTo(MarkupBuilder) Closure closure = {}) {
        removePlugin(groupId, artifactId)
        addPlugin(groupId, artifactId, version, runOnMarkupBuilder(closure))
    }

    private Node findManagedDependency(String groupId, String artifactId) {
        rootNode["dependencyManagement"]["dependencies"]["dependency"].find { dependency ->
            (dependency as Node)[GROUP_ID_QNAME].text() == groupId && (dependency as Node)[ARTIFACT_ID_QNAME].text() == artifactId
        } as Node
    }

    private Node findDependency(String groupId, String artifactId) {
        rootNode["dependencies"]["dependency"].find { dependency ->
            (dependency as Node)[GROUP_ID_QNAME].text() == groupId && (dependency as Node)[ARTIFACT_ID_QNAME].text() == artifactId
        } as Node
    }

    private Node findPlugin(String groupId, String artifactId) {
        rootNode["build"]["plugins"]["plugin"].find { plugin ->
            (plugin as Node)[GROUP_ID_QNAME].text() == groupId && (plugin as Node)[ARTIFACT_ID_QNAME].text() == artifactId
        } as Node
    }

    Pom addManagedDependency(String groupId, String artifactId, String version, String scope = 'import', String type = "pom", boolean checkForExistingDependency = true, String config = "") {
        Node dependency = findManagedDependency(groupId, artifactId)

        if (dependency && checkForExistingDependency) {
            assert dependency[VERSION_QNAME].text() == version
            assert dependency[SCOPE_QNAME].text() == scope

            if (type) {
                assert dependency[TYPE_QNAME].text() == type
            }

            if (config) {
                mergeXmlAt(dependency, config)
            }
        } else {
            addXmlAt('dependencyManagement/dependencies', """\
            <dependency>
                <groupId>$groupId</groupId>
                <artifactId>$artifactId</artifactId>
                ${version ? "<version>$version</version>" : ""}
                <scope>$scope</scope>
                ${type ? "<type>$type</type>" : ""}
                $config
            </dependency>""".stripIndent(true))
        }

        return this
    }

    Pom addDependency(String groupId, String artifactId, String version, String scope = 'compile', String type = "", boolean checkForExistingDependency = true, String config = "") {
        Node dependency = findDependency(groupId, artifactId)

        if (dependency && checkForExistingDependency) {
            assert dependency[VERSION_QNAME].text() == version
            assert dependency[SCOPE_QNAME].text() == scope

            if (type) {
                assert dependency[TYPE_QNAME].text() == type
            }

            if (config) {
                mergeXmlAt(dependency, config)
            }
        } else {
            addXmlAt('dependencies', """\
            <dependency>
                <groupId>$groupId</groupId>
                <artifactId>$artifactId</artifactId>
                ${version ? "<version>$version</version>" : ""}
                <scope>$scope</scope>
                ${type ? "<type>$type</type>" : ""}
                $config
            </dependency>""".stripIndent(true))
        }

        return this
    }

}
