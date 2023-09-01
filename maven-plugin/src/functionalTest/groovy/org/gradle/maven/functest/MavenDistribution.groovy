package org.gradle.maven.functest


import io.takari.maven.testing.executor.MavenExecutionResult
import io.takari.maven.testing.executor.MavenRuntime

import java.util.stream.Collectors

/**
 * Determines available maven distributions from the system property set from the build script.
 */
class MavenDistribution {

    private static final String TEST_CONTEXT_PREFIX = "testContext.internal.mavenHome."

    static List<MavenDistribution> allDistributions() {
        return System.getProperties().entrySet().stream()
            .filter { it.key.toString().startsWith(TEST_CONTEXT_PREFIX) }
            .map { it.value.toString() }
            .map { new File(it) }
            .map { new MavenDistribution(it) }
            .collect(Collectors.toList())
    }

    private final File mavenHome

    MavenDistribution(File mavenHome) {
        // points to a maven home. will have the format
        // /<user.home>/.gradle/caches/transforms-3/331b5717a2aee1ea51c043eab0ba4c93/transformed/apache-maven-3.8.7-bin/apache-maven-3.8.7
        this.mavenHome = mavenHome
    }

    MavenExecutionResult execute(MavenWorkspace workspace, String... goals) {
        workspace.ensureMaterialized()
        def forkedRunner = MavenRuntime.forkedBuilder(mavenHome).build()
        return forkedRunner.forProject(workspace.path.toFile()).execute(goals)
    }

    String getVersion() {
        def i = name.lastIndexOf('-')
        return name.substring(i + 1)
    }

    File getMavenHome() {
        return mavenHome
    }

    String getName() {
        return mavenHome.name
    }

    @Override
    String toString() {
        return "maven-" + version
    }
}
