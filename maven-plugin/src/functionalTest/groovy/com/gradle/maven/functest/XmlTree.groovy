/*
 * Copyright 2025 the original author or authors.
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
package com.gradle.maven.functest

import groovy.namespace.QName
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.xml.MarkupBuilder
import groovy.xml.XmlParser
import groovy.xml.XmlUtil

@CompileStatic
@EqualsAndHashCode
class XmlTree {

    private boolean invalid
    private final Node rootNode

    XmlTree(String rootName) {
        this(new Node(null, rootName))
    }

    XmlTree(Node rootNode) {
        this.rootNode = rootNode
    }

    protected Node getRootNode() {
        return rootNode
    }

    XmlTree addXmlAt(String path, String xml) {
        runAt(path) { node -> appendXmlAt(xml, node) }
        return this
    }

    protected XmlTree mergeXmlAt(Node parent, String xml) {
        def newNodes = fromString(xml)
        newNodes.each {
            def newNode = it as Node
            def existingNodes = parent[new QName(newNode.name() as String)]
            if (existingNodes.empty) {
                parent.append(newNode)
            } else {
                existingNodes.each { existingNode ->
                    newNode.children().each { child ->
                        (existingNode as Node).append(child as Node)
                    }
                }
            }
        }
        return this
    }

    protected final void runAt(String path, @ClosureParams(value = SimpleType, options = ["groovy.util.Node"]) Closure<?> closure) {
        def segments = path
            .split("/")
            .findAll { it } // filter out empty segments
        def p = segments.inject(rootNode) { Node parent, String name -> ensureNode(name, parent) } as Node
        closure.call(p)
    }

    protected static String runOnMarkupBuilder(@DelegatesTo(MarkupBuilder) Closure<Void> closure) {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        closure.setDelegate(builder)
        closure.call()
        writer.toString()
    }

    private static Node ensureNode(String name, Node parent) {
        Node n = parent.children().find {
            Node node = it as Node
            if (node.name() instanceof String) {
                node.name() == name
            } else {
                QName qName = node.name() as QName
                qName.localPart == name
            }
        } as Node
        n ?: parent.appendNode(name)
    }

    private static void appendXmlAt(String xml, Node node) {
        def nodes = fromString(xml)
        nodes.each { node.append(it as Node) }
    }

    private static void putXmlAt(String xml, Node node) {
        def nodes = fromString(xml)
        if (nodes == null) {
            node.parent().remove(node)
        } else {
            nodes.each { node.replaceNode(it as Node) }
        }
    }

    private static NodeList fromString(String xml) {
        xml ? new XmlParser().parseText("<parent>$xml</parent>").children() as NodeList : null
    }

    @Override
    String toString() {
        invalid ? '<<invalid>XML>' : XmlUtil.serialize(rootNode)
    }
}
