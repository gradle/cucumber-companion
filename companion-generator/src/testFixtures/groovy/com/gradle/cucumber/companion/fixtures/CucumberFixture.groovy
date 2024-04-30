/*
 * Copyright 2024 the original author or authors.
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
    List<ExpectedCompanionFile> expectedCompanionFiles(String suffix = '', boolean allowEmptySuites = false, List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        features.collect {
            ExpectedCompanionFile.create(it.featureName, it.contentHash, it.packageName, suffix, allowEmptySuites)
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

    void createPostDiscoveryFilter(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/java/org/junit/platform/launcher") {
                file("TestPostDiscoveryFilter.java").text = """
                    package org.junit.platform.launcher;
                    
                    import org.junit.platform.engine.*;
                    import org.junit.platform.engine.support.descriptor.*;
                    import org.junit.platform.launcher.PostDiscoveryFilter;
                    import java.util.*;
                    
                    // allows only one, pre-defined class name in the discovery phase
                    public class TestPostDiscoveryFilter implements PostDiscoveryFilter {
                    
                        private static final String FILTERED_CLASS_NAME = "user.User_Profile";
                    
                        public FilterResult apply(TestDescriptor testDescriptor) {
                            if(testDescriptor.getSource().isPresent()) {
                                TestSource testSource = testDescriptor.getSource().get();
                                // looking for FQCN of features and their parents
                                String className = testSource.toString();
                                // need also to consider 'feature' children of the suite
                                if(testSource instanceof ClassSource) {
                                    className = ((ClassSource) testSource).getClassName();
                                } else if(testSource instanceof ClasspathResourceSource) {
                                    String classpathResourceName = ((ClasspathResourceSource) testSource).getClasspathResourceName();
                                    className = classpathResourceName
                                        .replace('/', '.')
                                        .replace(".feature", "")
                                        .replaceAll("[^a-zA-Z0-9_\\\\.]", "_");
                                }
                                return FilterResult.includedIf(FILTERED_CLASS_NAME.equalsIgnoreCase(className));
                            }
                            return FilterResult.excluded("Suite/Feature name doesn't match");
                        }
                    }
                """
            }
        }
    }

    void registerPostDiscoveryFilter(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/resources/META-INF/services") {
                file("org.junit.platform.launcher.PostDiscoveryFilter").text = "org.junit.platform.launcher.TestPostDiscoveryFilter"
            }
        }
    }
}
