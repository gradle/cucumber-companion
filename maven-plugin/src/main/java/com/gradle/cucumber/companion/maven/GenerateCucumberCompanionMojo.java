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
package com.gradle.cucumber.companion.maven;

import com.gradle.cucumber.companion.generator.CompanionFile;
import com.gradle.cucumber.companion.generator.CompanionGenerator;
import com.gradle.cucumber.companion.generator.GeneratedClassOptions;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mojo(name = "generate-cucumber-companion-files", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateCucumberCompanionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.testResources}", required = true, readonly = true)
    private List<Resource> testResources;

    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/cucumberCompanion", required = true)
    private String generatedSourcesDirectory;
    private Path generatedSourcesDirectoryInternal;

    @Parameter(defaultValue = "Test", required = true)
    private String generatedFileNameSuffix;

    @Parameter(defaultValue = "false")
    private boolean allowEmptySuites;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private GeneratedClassCustomization customizeGeneratedClasses;

    private GeneratedClassOptions generatedClassOptions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generatedSourcesDirectoryInternal = Paths.get(generatedSourcesDirectory);
        generatedClassOptions = customizeGeneratedClasses == null
            ? new GeneratedClassOptions(
            Optional.empty(),
            Collections.emptyList(),
            Collections.emptyList(),
            allowEmptySuites
        )
            : new GeneratedClassOptions(
            Optional.ofNullable(customizeGeneratedClasses.getBaseClass()).filter(it -> !it.trim().isEmpty()),
            customizeGeneratedClasses.getInterfaces(),
            customizeGeneratedClasses.getAnnotations(),
            allowEmptySuites
        );
        getLog().info("Generating Cucumber companion files...");
        cleanOutputDirectory();
        ensureOutputDirectoryExists();
        createCompanionFiles();
        addGeneratedTestSourceDirectory();
    }

    private void addGeneratedTestSourceDirectory() {
        project.addTestCompileSourceRoot(generatedSourcesDirectoryInternal.toAbsolutePath().toString());
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
        if (Files.exists(generatedSourcesDirectoryInternal)) {
            try {
                deleteRecursively(generatedSourcesDirectoryInternal);
            } catch (IOException e) {
                throw new MojoExecutionException("Could not clean output directory", e);
            }
        }
    }

    private void ensureOutputDirectoryExists() throws MojoExecutionException {
        try {
            Files.createDirectories(generatedSourcesDirectoryInternal);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create output directory", e);
        }
    }

    private int createCompanionFiles(Path testResourcesDirectory) throws MojoExecutionException {
        if (!Files.exists(testResourcesDirectory)) {
            return 0;
        }

        try (Stream<Path> stream = Files.walk(testResourcesDirectory)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".feature"))
                .map(p -> new CompanionFile(testResourcesDirectory, generatedSourcesDirectoryInternal, p, generatedFileNameSuffix))
                .mapToInt(companionFile -> {
                    try {
                        logDebug(() -> "Creating " + companionFile);
                        CompanionGenerator.create(companionFile, generatedClassOptions);
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
