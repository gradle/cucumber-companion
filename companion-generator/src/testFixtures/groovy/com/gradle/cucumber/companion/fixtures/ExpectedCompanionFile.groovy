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
import groovy.transform.Immutable
import groovy.transform.NamedVariant

@CompileStatic
@Immutable
class ExpectedCompanionFile {
    String featureName
    String relativePath
    String className
    String packageName
    String classPathResource
    String contentHash
    boolean allowEmptySuites
    String baseClass
    List<String> interfaces
    List<String> annotations

    @NamedVariant
    static ExpectedCompanionFile create(String featureName,
                                        String contentHash,
                                        String packageName,
                                        String suffix = '',
                                        boolean allowEmptySuites = false,
                                        String baseClass = null,
                                        List<String> interfaces = [],
                                        List<String> annotations = []) {
        def safeName = featureName.replaceAll(" ", "_") + suffix
        new ExpectedCompanionFile(
            featureName,
            [packageName.replaceAll(/\./, '/'), safeName + ".java"].findAll().join("/"),
            safeName,
            packageName,
            [packageName.replaceAll(/\./, '/'), featureName + ".feature"].findAll().join("/"),
            contentHash,
            allowEmptySuites,
            baseClass,
            interfaces,
            annotations
        )
    }
}
