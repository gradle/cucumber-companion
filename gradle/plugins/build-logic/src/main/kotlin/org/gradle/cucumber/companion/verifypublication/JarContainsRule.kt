package org.gradle.cucumber.companion.verifypublication

import org.gradle.api.Action
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile

class JarContainsRule() {
    private val fileRules: MutableList<FileMatchesRule> = mutableListOf()

    fun aFile(pathInJar: String, action: Action<FileMatchesRule>): JarContainsRule {
        val rule = FileMatchesRule(pathInJar)
        action.execute(rule)
        fileRules.add(rule)
        return this
    }

    fun aFile(pathInJar: String): JarContainsRule {
        return aFile(pathInJar) { true }
    }

    fun verify(mainJarPath: RegularFile, jarContents: FileTree) {
        fileRules.forEach { it.verify(mainJarPath, jarContents) }
    }
}
