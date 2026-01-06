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
package com.gradle.maven.functest

import spock.util.io.FileSystemFixture

import java.nio.file.Path

class MavenWorkspace {

    FileSystemFixture fileSystem
    Path rootPom
    Closure<?> dirSpec = {}
    Pom pom = new Pom("func-test")
    boolean materialized

    MavenWorkspace(Path tempDir) {
        this.fileSystem = new FileSystemFixture(tempDir)
        this.rootPom = fileSystem.resolve("pom.xml")
    }

    MavenWorkspace pom(@DelegatesTo(value = Pom.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        pom.with(closure)
        this
    }

    def ensureMaterialized() {
        if (!materialized) {
            fileSystem.create(dirSpec)
            rootPom.text = pom.toString()
            materialized = true
        }
    }
}
