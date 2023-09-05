package org.gradle.maven.functest

import spock.util.io.FileSystemFixture

import java.nio.file.Path

class MavenWorkspace {

    FileSystemFixture fileSystem
    Path rootPom
    Closure<?> dirSpec = {}
    Pom pom = new Pom()
    boolean materialized

    MavenWorkspace(Path tempDir) {
        this.fileSystem = new FileSystemFixture(tempDir)
        this.rootPom = fileSystem.resolve("pom.xml")
    }


    MavenWorkspace pom(@DelegatesTo(value = Pom.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        pom.with(closure)
        this
    }

    def ensureMaterialized() {
        if (!materialized) {
            fileSystem.create(dirSpec)
            rootPom.text = pom.toString()

            println rootPom.text
            materialized = true
        }
    }

}
