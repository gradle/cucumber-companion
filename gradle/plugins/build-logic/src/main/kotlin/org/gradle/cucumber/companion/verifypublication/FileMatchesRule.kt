package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile

class FileMatchesRule(private val pathInJar: String) {
    private var contentMatcher: (content: String) -> Boolean = { true }

    fun matching(contentMatcher: (content: String) -> Boolean): FileMatchesRule {
        val currentMatcher = this.contentMatcher
        this.contentMatcher = { currentMatcher(it) && contentMatcher(it) }
        return this
    }

    fun verify(mainJarPath: RegularFile, jarContents: FileTree) {
        val match = jarContents.matching { include(pathInJar) }
        require(!match.isEmpty) { "Jar file $mainJarPath does not contain expected file '$pathInJar'" }
        val content = match.singleFile.readText()
        require(contentMatcher(content)) { "Content of file '$pathInJar' does not match" }
    }
}
