/*
 * Copyright 2026 the original author or authors.
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

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Function

@CompileStatic
class CompanionAssertions {
    Function<ExpectedCompanionFile, Path> fileResolver

    CompanionAssertions(Function<ExpectedCompanionFile, Path> fileResolver) {
        this.fileResolver = fileResolver
    }

    void assertCompanionFile(ExpectedCompanionFile companion) {
        Path companionFile = fileResolver.apply(companion)
        assert Files.exists(companionFile)
        def expected = """${companion.packageName ? "                    package $companion.packageName;\n\n" : ''}\
                    @org.junit.platform.suite.api.Suite${companion.allowEmptySuites ? "(failIfNoTests = false)": ''}
                    @org.junit.platform.suite.api.SelectClasspathResource("${companion.classPathResource}")${
            companion.annotations ? "\n${companion.annotations.collect { "                    $it" }.join('\n')}" : ''
        }
                    class ${companion.className}${companion.baseClass ? " extends $companion.baseClass" : ''}${companion.interfaces ? " implements ${companion.interfaces.join(', ')}" : ''} {
                        public static final String CONTENT_HASH = "${companion.contentHash}";
                    }
                    """.stripIndent(true)
        assert companionFile.text == expected
    }
}
