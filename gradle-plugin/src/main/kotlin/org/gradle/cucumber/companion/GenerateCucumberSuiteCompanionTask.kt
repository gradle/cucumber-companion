package org.gradle.cucumber.companion

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText


abstract class GenerateCucumberSuiteCompanionTask : DefaultTask() {

    @get:InputDirectory
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cucumberFeatureSources: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generateSuiteCompanionClasses() {
        val outputDir = outputDirectory.get().asFile.toPath()
        val inputDir = cucumberFeatureSources.get().asFile.toPath()
        Files.walk(inputDir).use { stream ->
            stream.filter { it.fileName.toString().endsWith(".feature") }.forEach {
                val featureName = removeFeatureExtension(it.name)
                val relative = inputDir.relativize(it)
                var packageName = ""
                var classPathResource = it.name
                var parent = outputDir
                if (relative.parent != null) {
                    parent = parent.resolve(relative.parent)
                    packageName = "package ${relative.parent.joinToString( ".")};"
                    classPathResource = "${relative.parent.joinToString("/")}/${it.name}"
                }
                if (!parent.exists()) {
                    Files.createDirectories(parent)
                }
                parent.resolve("$featureName.java")
                    .writeText(
                        """
                        $packageName

                        import org.junit.platform.suite.api.SelectClasspathResource;
                        import org.junit.platform.suite.api.Suite;

                        @Suite
                        @SelectClasspathResource("$classPathResource")
                        class $featureName {
                        }

                        """.trimIndent()
                    )
                it.name
            }
        }
    }

    private fun removeFeatureExtension(name: String) = name.substring(0, name.length - 8)
}
