package org.gradle.cucumber.companion

import groovy.transform.TupleConstructor
import org.gradle.cucumber.companion.testcontext.TestContext
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.gradle.util.internal.DefaultGradleVersion

import java.nio.file.Path

@TupleConstructor
class TestContextRunner {
    static final GradleVersion GRADLE_VERSION = DefaultGradleVersion.version(TestContext.getRequiredValue("gradleVersion"))
    static final boolean CONFIGURATION_CACHE = TestContext.getRequiredBoolean("configurationCache")

    final Path workspaceRoot

    BuildResult run(String... arguments) {
        def runner = createRunner(arguments)
        return runner.build()
    }

    BuildResult runAndFail(String... arguments) {
        def runner = createRunner(arguments)
        return runner.buildAndFail()
    }

    private GradleRunner createRunner(String... arguments) {
        List args = arguments as List
        if (CONFIGURATION_CACHE) {
            if (!(args.contains('--configuration-cache') || args.contains('--configuration-cache'))) {
                args << '--configuration-cache'
            }
        }
        def runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(workspaceRoot.toFile())
            .withGradleVersion(GRADLE_VERSION.version)
            .withArguments(args)
        runner
    }


}
