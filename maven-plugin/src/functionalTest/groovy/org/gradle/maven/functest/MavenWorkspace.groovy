package org.gradle.maven.functest

import spock.util.io.FileSystemFixture

import java.nio.file.Path

class MavenWorkspace {

    FileSystemFixture workspace
    Path rootPom
    Closure<?> dirSpec = {}
    def pom = new Pom()
    boolean materialized

    MavenWorkspace(Path tempDir) {
        this.workspace = new FileSystemFixture(tempDir)
        this.rootPom = workspace.resolve("pom.xml")
    }

    Path getPath() {
        return workspace.currentPath
    }

    FileSystemFixture getFileSystem() {
        return workspace
    }

    def resolve(String path) {
        return workspace.resolve(path)
    }

    def pom(@DelegatesTo(value = Pom.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        closure.setDelegate(pom)
        closure.call(pom)
        this
    }

    def ensureMaterialized() {
        if (!materialized) {
            workspace.create(dirSpec)
            rootPom.text = pom.toString()
            materialized = true
        }
    }

}