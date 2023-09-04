package org.gradle.cucumber.companion.maven

import io.takari.maven.testing.executor.MavenExecutionResult
import io.takari.maven.testing.executor.MavenRuntime
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Path
import java.util.stream.Collectors

abstract class BaseMavenFuncTest extends Specification {

    private static final String TEST_CONTEXT_PREFIX = "testContext.internal.mavenHome."

    @TempDir
    FileSystemFixture workspace
    Path pom

    def setup() {
        pom = workspace.file("pom.xml")
    }

    MavenExecutionResult execute(MavenDistribution distribution, String... goals) {
        def forkedRunner = MavenRuntime.forkedBuilder(distribution.mavenHome).build()
        return forkedRunner.forProject(workspace.currentPath.toFile()).execute(goals)
    }

    List<MavenDistribution> distributions() {
        return System.getProperties().entrySet().stream()
            .filter { it.key.toString().startsWith(TEST_CONTEXT_PREFIX) }
            .map { it.value.toString() }
            .map { new File(it) }
            .map { new MavenDistribution(it) }
            .collect(Collectors.toList())
    }

    static class MavenDistribution {
        private final File mavenHome

        MavenDistribution(File mavenHome) {
            this.mavenHome = mavenHome
        }

        String getName() {
            return mavenHome.getName()
        }

        @Override
        String toString() {
            return getName()
        }
    }
}
