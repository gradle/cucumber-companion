/*
 * Copyright 2025 the original author or authors.
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
package com.gradle.cucumber.companion

import com.gradle.cucumber.companion.testcontext.TestContext
import groovy.transform.TupleConstructor
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.gradle.util.internal.DefaultGradleVersion

import java.nio.file.Path
import java.nio.file.Files

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
            if (!(args.contains('--configuration-cache') || args.contains('--no-configuration-cache'))) {
                args << '--configuration-cache'
            }
        }

        writeJavaHomeToGradleProperties()

        def runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(workspaceRoot.toFile())
            .withGradleVersion(GRADLE_VERSION.version)
            .withArguments(args)
        runner
    }

    private void writeJavaHomeToGradleProperties() {
        def gradlePropertiesFile = workspaceRoot.resolve("gradle.properties")
        def properties = loadProperties(gradlePropertiesFile)
        properties.setProperty("org.gradle.java.home", jdkPathForGradleVersion(GRADLE_VERSION))
        gradlePropertiesFile.withWriter {
            properties.forEach((key, value) -> {
                it.writeLine("${key}=${escapeFilePath(value as String)}")
            })
        }
    }

    private static Properties loadProperties(Path gradlePropertiesFile) {
        new Properties().tap { properties ->
            if (Files.exists(gradlePropertiesFile)) {
                gradlePropertiesFile.withReader {
                    properties.load(it)
                }
            } else {
                Files.createFile(gradlePropertiesFile)
            }
        }
    }

    private static String jdkPathForGradleVersion(GradleVersion version) {
        if (version >= GradleVersion.version("9.0.0")) {
            System.getenv("JDK17")
        } else {
            System.getenv("JDK8")
        }
    }

    private static String escapeFilePath(String s) {
        return s?.replace("\\", "\\\\")
    }

}
