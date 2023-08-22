package org.gradle.cucumber.companion.generator;

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
    private final Path sourceDir;
    private final Path targetDir;
    private final Path actual;
    private final Path relativeSrc;
    private final Path relativeDest;
    private final Path destination;
    private final String featureName;
    private final Optional<String> packageName;

    public CompanionFile(Path sourceDir, Path destinationDir, Path actual) {
        if (!actual.getFileName().toString().endsWith(".feature")) {
            throw new IllegalArgumentException("The passed parameter was not a feature file:" + actual);
        }
        this.sourceDir = sourceDir;
        this.targetDir = destinationDir;
        this.actual = actual;
        this.relativeSrc = sourceDir.relativize(actual);
        this.featureName = toValidClassName(getNameWithoutExtension(actual));
        this.relativeDest = relativeSrc.resolveSibling(featureName + ".java");
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

    public Path getDestination() {
        return destination;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getClassPathResource() {
        return relativeDest.toString();
    }

    public Optional<String> getPackageName() {
        return packageName;
    }
}
