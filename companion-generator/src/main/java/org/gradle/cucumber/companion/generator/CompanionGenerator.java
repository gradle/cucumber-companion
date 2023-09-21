package org.gradle.cucumber.companion.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompanionGenerator {
    public static CompanionFile resolve(Path sourceDir, Path targetDir, Path actual) {
        return new CompanionFile(sourceDir, targetDir, actual);
    }

    public static void create(CompanionFile companionFile) throws IOException {
        // We could have included a templating engine, but it is just a few lines
        // and adding the dependency seems overkill.
        ensureParentDirectoryExists(companionFile.getDestination());
        try (BufferedWriter bw = Files.newBufferedWriter(companionFile.getDestination(), StandardCharsets.UTF_8)) {
            if (companionFile.getPackageName().isPresent()) {
                bw.write("package ");
                bw.write(companionFile.getPackageName().get());
                bw.write(";");
                bw.newLine();
                bw.newLine();
            }
            bw.write("@org.junit.platform.suite.api.Suite");
            bw.newLine();
            bw.write("@org.junit.platform.suite.api.SelectClasspathResource(\"");
            bw.write(companionFile.getClassPathResource());
            bw.write("\")");
            bw.newLine();
            bw.write("class ");
            bw.write(companionFile.getFeatureName());
            bw.write(" {}");
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureParentDirectoryExists(Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
    }
}
