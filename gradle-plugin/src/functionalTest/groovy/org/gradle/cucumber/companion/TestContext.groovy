package org.gradle.cucumber.companion

import org.gradle.util.GradleVersion
import org.gradle.util.internal.DefaultGradleVersion

class TestContext {
    static final GradleVersion gradleVersion = DefaultGradleVersion.version(contextProperty("gradleVersion"))
    static final boolean configurationCache = Boolean.parseBoolean(contextProperty("configurationCache"))

    static String contextProperty(String key) {
        System.getProperty("testContext.internal.$key")
    }

    static String asName() {
        return "[gradle: ${gradleVersion.version}, cc: $configurationCache]"
    }
}
