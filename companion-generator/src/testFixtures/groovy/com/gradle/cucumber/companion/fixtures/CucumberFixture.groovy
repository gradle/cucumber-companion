/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gradle.cucumber.companion.fixtures

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import spock.util.io.FileSystemFixture

@SuppressWarnings("GrMethodMayBeStatic")
@CompileStatic
class CucumberFixture {

    @Memoized
    List<ExpectedCompanionFile> expectedCompanionFiles(String suffix = '', List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        features.collect {
            ExpectedCompanionFile.create(it.featureName, it.contentHash, it.packageName, suffix)
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
