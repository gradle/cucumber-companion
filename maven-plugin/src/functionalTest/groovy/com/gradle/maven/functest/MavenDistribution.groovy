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
package com.gradle.maven.functest

import groovy.transform.Memoized
import io.takari.maven.testing.executor.MavenExecutionResult
import io.takari.maven.testing.executor.MavenRuntime

import java.util.stream.Collectors

/**
 * Determines available maven distributions from the system property set from the build script.
 */
class MavenDistribution {

    private static final String TEST_CONTEXT_PREFIX = "testContext.internal.mavenHome."

    @Memoized
    static List<MavenDistribution> allDistributions() {
        return System.getProperties().entrySet().stream()
            .filter { it.key.toString().startsWith(TEST_CONTEXT_PREFIX) }
            .map { it.value.toString() }
            .map { new File(it) }
            .map { new MavenDistribution(it) }
            .collect(Collectors.toList())
    }

    static MavenDistribution theSingleMavenDistribution() {
        def distributions = allDistributions()
        assert !distributions.empty: "No maven distributions found. Provide the path to a maven home directory via environment variable ${TEST_CONTEXT_PREFIX}.<identifier>"
        return distributions.get(0)
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
        return forkedRunner.forProject(workspace.fileSystem.currentPath.toFile()).execute(goals)
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
