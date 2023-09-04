package org.gradle.cucumber.companion.fixtures

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import spock.util.io.FileSystemFixture

@SuppressWarnings("GrMethodMayBeStatic")
@CompileStatic
class CucumberFixture {

    @Memoized
    List<ExpectedCompanionFile> expectedCompanionFiles(String suffix = '') {
        CucumberFeature.values().collect {
            ExpectedCompanionFile.create(it.featureName, it.packageName, suffix)
        }
    }

    void createFeatureFiles(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/resources") {
                CucumberFeature.values().each {
                    file(it.featureFilePath).text = it.featureFileContent
                }
            }
        }
    }

    void createStepFiles(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/java") {
                CucumberFeature.values().each {
                    file(it.stepFilePath).text = it.stepFileContent
                }
            }
        }
    }
}
