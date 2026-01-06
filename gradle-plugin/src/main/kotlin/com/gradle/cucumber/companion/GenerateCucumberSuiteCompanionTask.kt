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

import com.gradle.cucumber.companion.generator.CompanionGenerator
import com.gradle.cucumber.companion.generator.GeneratedClassOptions
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import java.nio.file.Files
import java.util.Optional

abstract class GenerateCucumberSuiteCompanionTask : DefaultTask() {

    @get:[SkipWhenEmpty InputFiles IgnoreEmptyDirectories PathSensitive(PathSensitivity.RELATIVE)]
    abstract val cucumberFeatureSources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val allowEmptySuites: Property<Boolean>

    @get:Nested
    abstract val customizeGeneratedClasses: GeneratedClassCustomization

    fun customizeGeneratedClasses(action: Action<GeneratedClassCustomization>) {
        action.execute(customizeGeneratedClasses)
    }

    @TaskAction
    fun generateSuiteCompanionClasses(inputChanges: InputChanges) {
        val outputDir = outputDirectory.get().asFile.toPath()
        val inputDirs = cucumberFeatureSources.files.map { it.toPath() }.filter { Files.exists(it) }
        inputDirs.forEach {
            if (!Files.isDirectory(it)) {
                throw IllegalArgumentException("Cucumber feature sources must be directories, but $it is a file.")
            }
        }
        val generatedClassOptions = GeneratedClassOptions(
            Optional.ofNullable(customizeGeneratedClasses.baseClass.orNull),
            customizeGeneratedClasses.interfaces.get(),
            customizeGeneratedClasses.annotations.get(),
            allowEmptySuites.get()
        )
        inputChanges.getFileChanges(cucumberFeatureSources).filter { it.file.name.toString().endsWith(".feature") }
            .forEach { change ->
                val actual = change.file.toPath()
                val inputDir = inputDirs.find { actual.startsWith(it) }
                    ?: throw IllegalArgumentException("File ${change.file} must be in one of the 'cucumberFeatureSources' directories to be processed by the 'GenerateCucumberSuiteCompanionTask'.")
                val companionFile = CompanionGenerator.resolve(inputDir, outputDir, actual)
                when (change.changeType) {
                    ChangeType.ADDED -> CompanionGenerator.create(companionFile, generatedClassOptions)
                    ChangeType.MODIFIED -> {
                        Files.deleteIfExists(companionFile.destination)
                        CompanionGenerator.create(companionFile, generatedClassOptions)
                    }
                    ChangeType.REMOVED -> Files.deleteIfExists(companionFile.destination)
                    else -> {}
                }
            }
    }
}
