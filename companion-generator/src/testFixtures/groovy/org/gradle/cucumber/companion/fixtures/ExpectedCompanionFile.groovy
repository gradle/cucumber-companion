package org.gradle.cucumber.companion.fixtures

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ExpectedCompanionFile {
    String featureName
    String relativePath
    String className
    String packageName
    String classPathResource

    static ExpectedCompanionFile create(String featureName, String packageName, String suffix = '') {
        def safeName = featureName.replaceAll(" ", "_") + suffix
        new ExpectedCompanionFile(
            featureName,
            [packageName.replaceAll(/\./, '/'), safeName + ".java"].findAll().join("/"),
            safeName,
            packageName,
            [packageName.replaceAll(/\./, '/'), featureName + ".feature"].findAll().join("/")
        )
    }
}
