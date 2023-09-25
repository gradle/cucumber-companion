package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree

class PublishedArtifactRule(private val name: String) {

    val filesInJar: MutableList<JarContainsRule> = mutableListOf()
    val classifiers: MutableSet<String> = mutableSetOf()
    var pomFileMatcher: (content: String) -> Boolean = { true }

    fun withClassifiers(vararg classifiers: String): PublishedArtifactRule {
        this.classifiers.addAll(classifiers)
        return this
    }

    fun withPomFileContentMatching(pomFileMatcher: (String) -> Boolean): PublishedArtifactRule {
        val currentMatcher = this.pomFileMatcher
        this.pomFileMatcher = { currentMatcher(it) && pomFileMatcher(it) }
        return this
    }

    fun withPomFileMatchingMavenCentralRequirements(): PublishedArtifactRule {
        // maven central requirements
        withPomFileContentMatching { content ->
            content.contains("<name>") &&
                content.contains("<url>") &&
                content.contains("<description>") &&
                content.contains("<license>") &&
                content.contains("<scm>")
        }
        return this
    }

    fun withJarContaining(jar: Action<JarContainsRule>): PublishedArtifactRule {
        var f = JarContainsRule()
        jar.execute(f)
        filesInJar.add(f)
        return this
    }

    fun verify(version: String, groupId: String, publicationBasePath: Directory, zip: (path: Any) -> FileTree) {
        val artifactBaseName = "${this.name}-$version"
        val baseDir = publicationBasePath.dir(name).dir(version)

        val publishedPom = baseDir.file("$artifactBaseName.pom")
        require(publishedPom.asFile.exists()) { "Missing pom file (Missing file: $publishedPom)" }
        require(pomFileMatcher(publishedPom.asFile.readText())) { "Pom file content doesn't match. (in $publishedPom)" }

        classifiers.forEach { classifier ->
            val expectedFileName = "$artifactBaseName${if (classifier.isEmpty()) "" else "-$classifier"}.jar"
            val expectedFile = baseDir.file(expectedFileName).asFile
            require(expectedFile.exists()) {
                "Expected a jar with classifier '$classifier' in $name. (Missing file: $expectedFile)"
            }
        }
        val mainJarPath = baseDir.file("$artifactBaseName.jar")
        val jarContents = zip(mainJarPath)
        filesInJar.forEach { it.verify(mainJarPath, jarContents) }
    }
}
