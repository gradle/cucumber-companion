package org.gradle.cucumber.companion.fixtures

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import spock.util.io.FileSystemFixture

@SuppressWarnings("GrMethodMayBeStatic")
@CompileStatic
class CucumberFixture {

    @Memoized
    List<ExpectedCompanionFile> expectedCompanionFiles(String suffix = '', List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        features.collect {
            ExpectedCompanionFile.create(it.featureName, it.packageName, suffix)
        }
    }

    void createFeatureFiles(FileSystemFixture projectDir, List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        projectDir.create {
            dir("src/test/resources") {
                features.each {
                    file(it.featureFilePath).text = it.featureFileContent
                }
            }
        }
    }

    void createStepFiles(FileSystemFixture projectDir, List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        projectDir.create {
            dir("src/test/java") {
                features.each {
                    file(it.stepFilePath).text = it.stepFileContent
                }
            }
        }
    }
}
