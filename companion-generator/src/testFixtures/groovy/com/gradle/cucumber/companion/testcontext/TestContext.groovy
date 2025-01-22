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
package com.gradle.cucumber.companion.testcontext

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
