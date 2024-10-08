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
package com.gradle.cucumber.companion.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GeneratedClassOptions {

    private final Optional<String> baseClass;
    private final List<String> interfaces;
    private final List<String> annotations;
    private final boolean allowEmptySuites;

    public GeneratedClassOptions(Optional<String> baseClass, List<String> interfaces, List<String> annotations, boolean allowEmptySuites) {
        this.baseClass = baseClass;
        this.interfaces = copyList(interfaces);
        this.annotations = copyList(annotations);
        this.allowEmptySuites = allowEmptySuites;
    }

    private static List<String> copyList(List<String> list) {
        return list.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(list));
    }

    public Optional<String> getBaseClass() {
        return baseClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public boolean isAllowEmptySuites() {
        return allowEmptySuites;
    }
}
