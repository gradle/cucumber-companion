package org.gradle.cucumber.companion.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.gradle.cucumber.companion.generator.CompanionFile;
import org.gradle.cucumber.companion.generator.CompanionGenerator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Mojo(name = "generate-cucumber-companion-files", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateCucumberCompanionMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true)
    private Path testResourcesDirectory;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/cucumberCompanion", required = true)
    private Path generatedSourcesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating Cucumber companion files...");
        cleanOutputDirectory();
        ensureOutputDirectoryExists();
        createCompanionFiles();
        getLog().info("Companion files generated successfully!");
    }

    private void cleanOutputDirectory() throws MojoExecutionException {
        if (Files.exists(generatedSourcesDirectory)) {
            try {
                deleteRecursively(generatedSourcesDirectory);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not clean output directory", e);
            }
        }
    }

    private void ensureOutputDirectoryExists() throws MojoExecutionException {
        try {
            Files.createDirectories(generatedSourcesDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create output directory", e);
        }
    }

    private void createCompanionFiles() throws MojoExecutionException {
        try (java.util.stream.Stream<Path> stream = Files.walk(testResourcesDirectory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".feature"))
                .map(p -> new CompanionFile(testResourcesDirectory, generatedSourcesDirectory, p))
                .forEach(companionFile -> {
                    try {
                        CompanionGenerator.create(companionFile);
                    } catch (IOException e) {
                        throw new RuntimeException("Could not create companion files.", e);
                    }
                });
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create companion files.", e);
        }
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        } else {
            Files.deleteIfExists(path);
        }
    }
}
