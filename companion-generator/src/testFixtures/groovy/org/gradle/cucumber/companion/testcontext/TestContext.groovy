package org.gradle.cucumber.companion.testcontext

class TestContext {

    private static final String PREFIX = "testContext.internal."
    private static final Map<String, String> CONTEXT = System.properties
            .findAll {  String k, String v -> k.startsWith(PREFIX) }
            .collectEntries { String k, String v -> [k.substring(PREFIX.length()), v] }

    static String getRequiredValue(String key) {
        def value = CONTEXT.get(key)
        if (value == null) {
            throw new IllegalStateException("TestContext does not contain a value for $key")
        }
        return value
    }

    static boolean getRequiredBoolean(String key) {
        return Boolean.valueOf(getRequiredValue(key))
    }

    static String asName() {
        return CONTEXT.toString()
    }
}
