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
package com.gradle.cucumber.companion

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface GeneratedClassCustomization {

    /**
     * Fully qualified name of the base class to extend.
     */
    @get:[Input Optional]
    val baseClass: Property<String>

    /**
     * Fully qualified name of the interfaces to implement.
     */
    @get:[Input Optional]
    val interfaces: ListProperty<String>

    /**
     * Fully qualified name of the annotations to add.
     * <p>
     * The value will be added as is, so make sure to include the {@code @} and that it is valid Java code.
     * <p>
     * <pre>{@code
     * annotations.add("@com.acme.MyAnnotation")
     * annotations.add("@com.acme.AnotherAnnotation(value = \"foo\")")
     * }
     */
    @get:[Input Optional]
    val annotations: ListProperty<String>
}
