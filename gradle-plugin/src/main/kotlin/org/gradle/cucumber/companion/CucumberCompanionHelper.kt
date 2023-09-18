package org.gradle.cucumber.companion

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer

fun generateCucumberSuiteCompanion(suite: JvmTestSuite, project: Project) {
    val taskContainer = project.tasks
    val buildDirectory = project.layout.buildDirectory
    generateCucumberSuiteCompanion(suite, taskContainer, buildDirectory)
}

fun generateCucumberSuiteCompanion(
    suite: JvmTestSuite,
    taskContainer: TaskContainer,
    buildDirectory: DirectoryProperty
) {
    val sourceSet = suite.sources
    generateCucumberSuiteCompanion(taskContainer, buildDirectory, sourceSet, suite.name)
}

fun generateCucumberSuiteCompanion(
    taskContainer: TaskContainer,
    buildDirectory: DirectoryProperty,
    sourceSet: SourceSet,
    name: String
) {
    val companionTask = taskContainer.register(
        "${name}GenerateCucumberSuiteCompanion",
        GenerateCucumberSuiteCompanionTask::class.java
    )
    val outputDir = buildDirectory.dir("generated-sources/cucumberCompanion-$name")

    companionTask.configure {
        // this is a bit icky, ideally we'd use a SourceDirectorySet ourselves, but I'm not sure that is proper
        this.cucumberFeatureSources.set(sourceSet.resources.srcDirs.first())
        this.outputDirectory.set(outputDir)
    }
    sourceSet.java.srcDir(outputDir)
    taskContainer.named(sourceSet.compileJavaTaskName) {
        this.dependsOn(companionTask.name)
    }
}
