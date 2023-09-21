package org.gradle.cucumber.companion.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.gradle.cucumber.companion.generator.CompanionFile;
import org.gradle.cucumber.companion.generator.CompanionGenerator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mojo(name = "generate-cucumber-companion-files", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateCucumberCompanionMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.testResources}", required = true, readonly = true)
    private List<Resource> testResources;

    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/cucumberCompanion", required = true)
    private String generatedSourcesDirectory;
    private Path _generatedSourcesDirectory;


    @Parameter(defaultValue = "Test", required = true)
    private String generatedFileNameSuffix;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        _generatedSourcesDirectory = Paths.get(generatedSourcesDirectory);
        getLog().info("Generating Cucumber companion files...");
        cleanOutputDirectory();
        ensureOutputDirectoryExists();
        createCompanionFiles();
        addGeneratedTestSourceDirectory();
    }

    private void addGeneratedTestSourceDirectory() {
        project.addTestCompileSourceRoot(_generatedSourcesDirectory.toAbsolutePath().toString());
    }

    private void createCompanionFiles() throws MojoExecutionException {
        int created = 0;
        for (Resource testResource : testResources) {
            Path path = Paths.get(testResource.getDirectory());
            logDebug(() -> "Scanning for cucumber features in: " + path);
            created += createCompanionFiles(path);
        }

        getLog().info("Generated " + created + " Companion files generated successfully!");
    }

    private void cleanOutputDirectory() throws MojoExecutionException {
        if (Files.exists(_generatedSourcesDirectory)) {
            try {
                deleteRecursively(_generatedSourcesDirectory);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not clean output directory", e);
            }
        }
    }

    private void ensureOutputDirectoryExists() throws MojoExecutionException {
        try {
            Files.createDirectories(_generatedSourcesDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create output directory", e);
        }
    }

    private int createCompanionFiles(Path _testResourcesDirectory) throws MojoExecutionException {
        if (!Files.exists(_testResourcesDirectory)) {
            return 0;
        }

        try (Stream<Path> stream = Files.walk(_testResourcesDirectory)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".feature"))
                .map(p -> new CompanionFile(_testResourcesDirectory, _generatedSourcesDirectory, p, generatedFileNameSuffix))
                .mapToInt(companionFile -> {
                    try {
                        logDebug(() -> "Creating " + companionFile);
                        CompanionGenerator.create(companionFile);
                        return 1;
                    } catch (IOException e) {
                        throw new RuntimeException("Could not create companion files.", e);
                    }
                }).sum();
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

    private void logDebug(Supplier<String> messageSupplier) {
        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug(messageSupplier.get());
        }
    }
}
