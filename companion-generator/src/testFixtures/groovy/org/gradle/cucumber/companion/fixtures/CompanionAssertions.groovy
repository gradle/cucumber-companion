package org.gradle.cucumber.companion.fixtures

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
                    @org.junit.platform.suite.api.Suite
                    @org.junit.platform.suite.api.SelectClasspathResource("${companion.classPathResource}")
                    class ${companion.className} {
                        public static final String CONTENT_HASH = "${companion.contentHash}";
                    }
                    """.stripIndent(true)
        assert companionFile.text == expected
    }
}
