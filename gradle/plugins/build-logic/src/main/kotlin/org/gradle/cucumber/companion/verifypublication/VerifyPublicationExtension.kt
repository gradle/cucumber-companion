package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import java.io.File

abstract class VerifyPublicationExtension {
    companion object {
        val NAME = "verifyPublication"
    }

    private val artifacts: MutableList<PublishedArtifactRule> = mutableListOf()

    @get:Input
    abstract val verificationRepoDir: DirectoryProperty

    fun expectPublishedArtifact(name: String, action: Action<PublishedArtifactRule>) {
        val pa = PublishedArtifactRule(name)
        action.execute(pa)
        artifacts.add(pa)
    }

    fun verify(version: String, groupId: String, zip: (path: Any) -> FileTree) {
        val publicationBasePath = verificationRepoDir
            .map { it.dir(groupId.replace(".", File.separator)) }
            .get()

        val files = publicationBasePath.asFile.list()?.size
        require(files == artifacts.size) { "Expected ${artifacts.size} published artifacts but was $files" }

        artifacts.forEach { it.verify(version, groupId, publicationBasePath, zip) }
    }
}
