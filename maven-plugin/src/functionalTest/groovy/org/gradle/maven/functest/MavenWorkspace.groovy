package org.gradle.maven.functest

import spock.util.io.DirectoryFixture
import spock.util.io.FileSystemFixture

import java.nio.file.Path

class MavenWorkspace {

    FileSystemFixture workspace
    Path rootPom
    Closure<?> dirSpec = {}
    def pom = new Pom()
    boolean materialized

    MavenWorkspace(FileSystemFixture workspace) {
        this.workspace = workspace
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
        closure.call(pom)
        this
    }

    def layout(@DelegatesTo(value = DirectoryFixture.class, strategy = Closure.DELEGATE_FIRST)
                   Closure<?> dirSpec) throws IOException {
        this.dirSpec = dirSpec
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
