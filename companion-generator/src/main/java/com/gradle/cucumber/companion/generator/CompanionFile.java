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
package com.gradle.cucumber.companion.generator;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CompanionFile {

    private static final Pattern VALID_PACKAGE_ELEMENTS = Pattern.compile("[^a-zA-Z0-9_$]");
    private static final Pattern VALID_CLASS_NAME = Pattern.compile("[^a-zA-Z0-9_]");
    private final Path actual;
    private final Path relativeSrc;
    private final Path destination;
    private final String featureName;
    private final Optional<String> packageName;

    public CompanionFile(Path sourceDir, Path destinationDir, Path actual) {
        this(sourceDir, destinationDir, actual, "");
    }

    public CompanionFile(Path sourceDir, Path destinationDir, Path actual, String suffix) {
        if (!actual.getFileName().toString().endsWith(".feature")) {
            throw new IllegalArgumentException("The passed parameter was not a feature file:" + actual);
        }
        this.actual = actual;
        this.relativeSrc = sourceDir.relativize(actual);
        this.featureName = toValidClassName(getNameWithoutExtension(actual)) + suffix;
        Path relativeDest = relativeSrc.resolveSibling(featureName + ".java");
        this.destination = destinationDir.resolve(relativeDest);
        this.packageName = relativeSrc.getParent() == null ? Optional.empty() : Optional.of(toPackageList(relativeSrc.getParent()));
    }

    private String toValidClassName(String nameWithoutExtension) {
        return VALID_CLASS_NAME.matcher(nameWithoutExtension).replaceAll("_");
    }

    private String toPackageList(Path parent) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(parent.iterator(), Spliterator.ORDERED), false)
            .map(Path::toString)
            .map(VALID_PACKAGE_ELEMENTS::matcher)
            .map(m -> m.replaceAll("_"))
            .collect(Collectors.joining("."));
    }

    private static String getNameWithoutExtension(Path path) {
        String originalFileName = path.getFileName().toString();
        return originalFileName.substring(0, originalFileName.lastIndexOf('.'));
    }

    public Path getSource() {
        return actual;
    }

    public Path getDestination() {
        return destination;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getClassPathResource() {
        return relativeSrc.toString();
    }

    public Optional<String> getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return "CompanionFile{src=" + actual + ", dest=" + destination + "}";
    }
}
