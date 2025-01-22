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
@file:Suppress("unused")

package org.gradle.kotlin.dsl

import com.gradle.cucumber.companion.CucumberCompanionExtension
import com.gradle.cucumber.companion.GenerateCucumberSuiteCompanionTask
import com.gradle.cucumber.companion.NoOpAction
import com.gradle.cucumber.companion.generateCucumberSuiteCompanion
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite

fun JvmTestSuite.generateCucumberSuiteCompanion(
    project: Project, configureTask: Action<GenerateCucumberSuiteCompanionTask> = NoOpAction
) = generateCucumberSuiteCompanion(this, project, project.extensions.getByType<CucumberCompanionExtension>(), configureTask)
