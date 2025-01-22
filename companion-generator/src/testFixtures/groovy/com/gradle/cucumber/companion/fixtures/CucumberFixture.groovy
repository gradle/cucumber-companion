/*
 * Copyright 2025 the original author or authors.
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
import groovy.transform.NamedVariant
import spock.util.io.FileSystemFixture

@SuppressWarnings("GrMethodMayBeStatic")
@CompileStatic
class CucumberFixture {

    @Memoized
    @NamedVariant
    List<ExpectedCompanionFile> expectedCompanionFiles(
        List<CucumberFeature> features = CucumberFeature.allSucceeding(),
        String suffix = '',
        boolean allowEmptySuites = false,
        String baseClass = null,
        List<String> interfaces = [],
        List<String> annotations = []) {

        features.collect {
            ExpectedCompanionFile.create(it.featureName, it.contentHash, it.packageName, suffix, allowEmptySuites, baseClass, interfaces, annotations)
        }
    }

    def createFeatureFiles(FileSystemFixture projectDir, List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        projectDir.create {
            dir("src/test/resources") {
                features.each {
                    file(it.featureFilePath).text = it.featureFileContent
                }
            }
        }
    }

    def createStepFiles(FileSystemFixture projectDir, List<CucumberFeature> features = CucumberFeature.allSucceeding()) {
        projectDir.create {
            dir("src/test/java") {
                features.each {
                    file(it.stepFilePath).text = it.stepFileContent
                }
            }
        }
    }

    def createBaseClass(FileSystemFixture projectDir, String baseClassName) {
        projectDir.create {
            dir("src/test/java") {
                file(toFileName(baseClassName)).text = """
                    ${extractPackageName(baseClassName)}                    
                   
                    public class ${extractClassName(baseClassName)} {
                    }
                """
            }
        }
    }

    def createInterface(FileSystemFixture projectDir, String interfaceName) {
        projectDir.create {
            dir("src/test/java") {
                file(toFileName(interfaceName)).text = """
                    ${extractPackageName(interfaceName)}                    
                   
                    public interface ${extractClassName(interfaceName)} {
                    }
                """
            }
        }
    }

    private String toFileName(String className) {
        className.replace('.', '/') + ".java"
    }

    private String extractPackageName(String className) {
        className.contains('.') ? 'package ' + className.substring(0, className.lastIndexOf('.')) + ';' : ''
    }

    private String extractClassName(String className) {
        className.contains('.') ? className.substring(className.lastIndexOf('.') + 1) : className
    }

    def createPostDiscoveryFilter(FileSystemFixture projectDir, String filteredClassName) {
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
                                return FilterResult.includedIf("${filteredClassName}".equalsIgnoreCase(className));
                            }
                            return FilterResult.excluded("Suite/Feature name doesn't match");
                        }
                    }
                """
            }
        }
    }

    def registerPostDiscoveryFilter(FileSystemFixture projectDir) {
        projectDir.create {
            dir("src/test/resources/META-INF/services") {
                file("org.junit.platform.launcher.PostDiscoveryFilter").text = "org.junit.platform.launcher.TestPostDiscoveryFilter"
            }
        }
    }
}
