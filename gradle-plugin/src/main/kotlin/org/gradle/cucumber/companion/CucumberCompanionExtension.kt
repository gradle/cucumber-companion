package org.gradle.cucumber.companion

import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskContainer
import javax.inject.Inject


abstract class CucumberCompanionExtension @Inject constructor(
    @Internal
    private val taskContainer: TaskContainer,
    @Internal
    private val projectLayout: ProjectLayout
) {
    companion object {
        val NAME = "cucumberCompanion"
    }

    @get:Input
    abstract val enableForStandardTestTask: Property<Boolean>

    init {
        enableForStandardTestTask.convention(true)
    }

    fun generateCucumberSuiteCompanion(suite: JvmTestSuite) {
        generateCucumberSuiteCompanion(suite, taskContainer, projectLayout.buildDirectory)
    }
}
