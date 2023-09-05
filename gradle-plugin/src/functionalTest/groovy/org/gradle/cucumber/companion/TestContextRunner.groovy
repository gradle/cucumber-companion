package org.gradle.cucumber.companion

import groovy.transform.TupleConstructor
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Path

@TupleConstructor
class TestContextRunner {

    final Path workspaceRoot


    BuildResult run(String... arguments) {
        List args = arguments as List
        if (TestContext.configurationCache) {
            if (!(args.contains('--configuration-cache') || args.contains('--configuration-cache'))) {
                args << '--configuration-cache'
            }
        }
        def runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(workspaceRoot.toFile())
                .withGradleVersion(TestContext.gradleVersion.version)
                .withArguments(args)

        return runner.build()
    }
}
