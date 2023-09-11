@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.cucumber.companion.generateCucumberSuiteCompanion

fun JvmTestSuite.generateCucumberSuiteCompanion(project: Project) {
    generateCucumberSuiteCompanion(this, project)
}
