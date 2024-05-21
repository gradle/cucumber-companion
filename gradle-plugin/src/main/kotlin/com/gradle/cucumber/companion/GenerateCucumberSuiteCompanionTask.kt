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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import com.gradle.cucumber.companion.generator.CompanionGenerator
import org.gradle.api.provider.Property
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import java.nio.file.Files


abstract class GenerateCucumberSuiteCompanionTask : DefaultTask() {

    @get:[SkipWhenEmpty InputDirectory IgnoreEmptyDirectories PathSensitive(PathSensitivity.RELATIVE)]
    abstract val cucumberFeatureSources: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val allowEmptySuites: Property<Boolean>

    init {
        allowEmptySuites.convention(false)
    }

    @TaskAction
    fun generateSuiteCompanionClasses(inputChanges: InputChanges) {
        val outputDir = outputDirectory.get().asFile.toPath()
        val inputDir = cucumberFeatureSources.get().asFile.toPath()
        inputChanges.getFileChanges(cucumberFeatureSources).filter { it.file.name.toString().endsWith(".feature") }
            .forEach { change ->
                val companionFile = CompanionGenerator.resolve(inputDir, outputDir, change.file.toPath())
                when (change.changeType) {
                    ChangeType.ADDED -> CompanionGenerator.create(companionFile, allowEmptySuites.get())
                    ChangeType.MODIFIED -> {
                        Files.deleteIfExists(companionFile.destination)
                        CompanionGenerator.create(companionFile, allowEmptySuites.get())
                    }
                    ChangeType.REMOVED -> Files.deleteIfExists(companionFile.destination)
                    else -> {}
                }
            }
    }
}
