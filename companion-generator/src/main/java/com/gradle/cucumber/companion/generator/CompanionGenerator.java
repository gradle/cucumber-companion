/*
 * Copyright 2023 the original author or authors.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
            bw.write(companionFile.getClassPathResource().replace('\\', '/'));
            bw.write("\")");
            bw.newLine();
            bw.write("class ");
            bw.write(companionFile.getFeatureName());
            bw.write(" {");
            bw.newLine();
            bw.write("    public static final String CONTENT_HASH = \"");
            bw.write(generateFileHash(companionFile.getSource()));
            bw.write("\";");
            bw.newLine();
            bw.write("}");
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureParentDirectoryExists(Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
    }

    private static String generateFileHash(Path actual) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (InputStream fis = Files.newInputStream(actual)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            byte[] hash = md.digest();
            return Base64.getUrlEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "<No SHA-256 algorithm available>";
        } catch (IOException e) {
            return "<Could not read source file>";
        }
    }
}
