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

    val artifacts: MutableList<PublishedArtifactRule> = mutableListOf()

    @get:Input
    abstract val verificationRepoDir: DirectoryProperty

    fun expectPublishedArtifact(name: String, action: Action<PublishedArtifactRule>) {
        val pa = PublishedArtifactRule(name)
        action.execute(pa)
        artifacts.add(pa)
    }


}
