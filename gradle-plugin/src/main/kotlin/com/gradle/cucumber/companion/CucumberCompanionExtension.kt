/*
 * Copyright 2024 the original author or authors.
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

import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import javax.inject.Inject

abstract class CucumberCompanionExtension @Inject constructor(
    @Internal
    private val taskContainer: TaskContainer,
    @Internal
    private val projectLayout: ProjectLayout
) {
    companion object {
        const val NAME = "cucumberCompanion"
    }

    @get:Input
    abstract val enableForStandardTestTask: Property<Boolean>

    @get:Input
    abstract val allowEmptySuites: Property<Boolean>

    @get:Nested
    abstract val customizeGeneratedClasses: GeneratedClassCustomization

    init {
        enableForStandardTestTask.convention(true)
        allowEmptySuites.convention(false)
    }

    fun customizeGeneratedClasses(action: Action<GeneratedClassCustomization>) {
        action.execute(customizeGeneratedClasses)
    }

    /**
     * Keep the function w/o additional task configuration action since groovy doesn't play nice
     * with default parameter values and is unable to find the method.
     */
    fun generateCucumberSuiteCompanion(
        suite: JvmTestSuite
    ) = generateCucumberSuiteCompanion(suite, NoOpAction)

    fun generateCucumberSuiteCompanion(
        suite: JvmTestSuite,
        configureTask: Action<GenerateCucumberSuiteCompanionTask>
    ) = generateCucumberSuiteCompanion(suite, taskContainer, projectLayout.buildDirectory, this, configureTask)
}
