package org.gradle.maven.functest

import spock.lang.Specification
import spock.lang.TempDir

abstract class BaseMavenFuncTest extends Specification {

    @TempDir
    MavenWorkspace workspace

}
