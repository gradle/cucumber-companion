/*
 * Copyright 2026 the original author or authors.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.testing.base.TestingExtension

class CucumberCompanionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            CucumberCompanionExtension::class.java,
            CucumberCompanionExtension.NAME,
            CucumberCompanionExtension::class.java
        )

        project.afterEvaluate {
            if (extension.enableForStandardTestTask.get()) {
                val name = "test"
                val testSuite =
                    project.extensions.findByType(TestingExtension::class.java)?.suites?.withType(JvmTestSuite::class.java)
                        ?.findByName(name)
                if (testSuite != null) {
                    generateCucumberSuiteCompanion(testSuite, project, extension)
                } else {
                    generateCucumberSuiteCompanion(
                        project.tasks,
                        project.layout.buildDirectory,
                        project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.named(name).get(),
                        name,
                        extension
                    )
                }
            }
        }
    }
}
