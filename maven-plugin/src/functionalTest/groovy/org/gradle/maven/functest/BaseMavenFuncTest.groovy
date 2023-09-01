package org.gradle.maven.functest


import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

abstract class BaseMavenFuncTest extends Specification {

    @TempDir
    FileSystemFixture tempWorkspace

    MavenWorkspace workspace

    def setup() {
        workspace = new MavenWorkspace(tempWorkspace)
    }

}
