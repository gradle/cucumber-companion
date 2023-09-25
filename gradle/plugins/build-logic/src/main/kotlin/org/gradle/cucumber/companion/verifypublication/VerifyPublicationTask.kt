package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
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
        verifyPublicationExtension.get().verify(version.get(), groupId.get()) { fileOps.zipTree(it) }
    }
}
