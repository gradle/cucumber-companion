package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class VerifyPublicationTask : DefaultTask() {

    @get:Inject
    abstract val fileOps: FileOperations

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val groupId: Property<String>

    @get:Input
    abstract val verifyPublicationExtension: Property<VerifyPublicationExtension>

    @TaskAction
    fun verify() {
        verifyPublishedArtifacts(version.get(), groupId.get())
    }

    private fun verifyPublishedArtifacts(version: String, groupId: String) {
        val extension = verifyPublicationExtension.get()
        val publicationBasePath = extension.verificationRepoDir
            .map { it.dir(groupId.replace(".", File.separator)) }
            .get()

        val artifacts = extension.artifacts
        val files = publicationBasePath.asFile.list()?.size
        require(files == artifacts.size) { "Expected ${artifacts.size} published artifacts but was $files" }

        artifacts.forEach { verifySingleArtifact(it, version, publicationBasePath) }
    }

    private fun verifySingleArtifact(
        artifact: PublishedArtifactRule,
        version: String,
        publicationBasePath: Directory
    ) {
        val artifactBaseName = "${artifact.name}-$version"
        val baseDir = publicationBasePath.dir(artifact.name).dir(version)

        val publishedPom = baseDir.file("$artifactBaseName.pom")
        require(publishedPom.asFile.exists()) { "Missing pom file: $publishedPom)" }

        val pomFileContent = publishedPom.asFile.readText()
        artifact.pomFileMatchers.forEach {
            require(it.matcher(pomFileContent)) { "${publishedPom.asFile.name}: content doesn't match: ${it.message}" }
        }

        artifact.classifiers.forEach { classifier ->
            val expectedFileName = "$artifactBaseName${if (classifier.isEmpty()) "" else "-$classifier"}.jar"
            val expectedFile = baseDir.file(expectedFileName).asFile
            require(expectedFile.exists()) {
                "Expected a jar with classifier '$classifier' in ${artifact.name}. (Missing file: $expectedFile)"
            }
        }
        val mainJarPath = baseDir.file("$artifactBaseName.jar")
        val jarContents = fileOps.zipTree(mainJarPath)
        artifact.filesInJar
            .flatMap { it.fileRules }
            .forEach { verifyFileInJar(it, mainJarPath, jarContents) }
    }

    private fun verifyFileInJar(fileInJar: FileMatchesRule, mainJarPath: RegularFile, jarContents: FileTree) {
        val match = jarContents.matching { include(fileInJar.pathInJar) }
        require(!match.isEmpty) { "Jar file $mainJarPath does not contain expected file '${fileInJar.pathInJar}'" }
        val content = match.singleFile.readText()
        fileInJar.contentMatcher.forEach {
            require(it.matcher(content)) { "${fileInJar.pathInJar}: content doesn't match: ${it.message}" }
        }
    }
}
