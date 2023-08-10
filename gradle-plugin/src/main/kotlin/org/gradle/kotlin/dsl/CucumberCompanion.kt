@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.cucumber.companion.GenerateCucumberSuiteCompanionTask

fun JvmTestSuite.generateCucumberSuiteCompanion(project: Project) {
    val suite = this
    val companionTask = project.tasks.register("${suite.name}GenerateCucumberSuiteCompanion", GenerateCucumberSuiteCompanionTask::class.java)
    val outputDir = project.layout.buildDirectory.dir("generated-sources/cucumberCompanion-${suite.name}")
    companionTask.configure {
        // this is a bit icky, ideally we'd use a SourceDirectorySet ourselves, but I'm not sure that is proper
        it.cucumberFeatureSources.set(sources.resources.srcDirs.first())
        it.outputDirectory.set(outputDir)
    }
    sources.java.srcDir(outputDir)
    project.tasks.named(sources.compileJavaTaskName) {
        it.dependsOn(companionTask.name)
    }
}
