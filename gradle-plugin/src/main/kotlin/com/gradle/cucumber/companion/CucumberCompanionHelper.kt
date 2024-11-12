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
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import java.io.Serializable

internal object NoOpAction : Action<GenerateCucumberSuiteCompanionTask>, Serializable {
    override fun execute(task: GenerateCucumberSuiteCompanionTask) {
    }
}

fun generateCucumberSuiteCompanion(
    suite: JvmTestSuite,
    project: Project,
    extension: CucumberCompanionExtension,
    configureTask: Action<GenerateCucumberSuiteCompanionTask> = NoOpAction
) {
    val taskContainer = project.tasks
    val buildDirectory = project.layout.buildDirectory
    generateCucumberSuiteCompanion(suite, taskContainer, buildDirectory, extension, configureTask)
}

fun generateCucumberSuiteCompanion(
    suite: JvmTestSuite,
    taskContainer: TaskContainer,
    buildDirectory: DirectoryProperty,
    extension: CucumberCompanionExtension,
    configureTask: Action<GenerateCucumberSuiteCompanionTask> = NoOpAction
) {
    val sourceSet = suite.sources
    generateCucumberSuiteCompanion(taskContainer, buildDirectory, sourceSet, suite.name, extension, configureTask)
}

fun generateCucumberSuiteCompanion(
    taskContainer: TaskContainer,
    buildDirectory: DirectoryProperty,
    sourceSet: SourceSet,
    name: String,
    extension: CucumberCompanionExtension,
    configureTask: Action<GenerateCucumberSuiteCompanionTask> = NoOpAction
) {
    val companionTask = taskContainer.register(
        "${name}GenerateCucumberSuiteCompanion",
        GenerateCucumberSuiteCompanionTask::class.java
    )
    val outputDir = buildDirectory.dir("generated-sources/cucumberCompanion-$name")

    companionTask.configure {
        this.cucumberFeatureSources.from(sourceSet.resources.sourceDirectories)
        this.outputDirectory.set(outputDir)
        allowEmptySuites.convention(extension.allowEmptySuites)
        customizeGeneratedClasses.baseClass.convention(extension.customizeGeneratedClasses.baseClass)
        customizeGeneratedClasses.annotations.convention(extension.customizeGeneratedClasses.annotations)
        customizeGeneratedClasses.interfaces.convention(extension.customizeGeneratedClasses.interfaces)
        configureTask.execute(this)
    }
    sourceSet.java.srcDir(companionTask)
}
