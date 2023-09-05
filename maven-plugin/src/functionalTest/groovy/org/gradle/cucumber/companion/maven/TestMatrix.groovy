package org.gradle.cucumber.companion.maven

import org.gradle.maven.functest.JDK
import org.gradle.maven.functest.MavenDistribution

class TestMatrix {

    private final Collection<JDK> javaVersions
    private final Collection<MavenDistribution> mavenVersions
    private final Collection<String> surefireVersions

    static TestMatrix of(Collection<JDK> javaVersions, Collection<MavenDistribution> mavenVersions, Collection<String> surefireVersions) {
        return new TestMatrix(javaVersions, mavenVersions, surefireVersions)
    }

    TestMatrix(Collection<JDK> javaVersions, Collection<MavenDistribution> mavenVersions, Collection<String> surefireVersions) {
        this.javaVersions = javaVersions
        this.mavenVersions = mavenVersions
        this.surefireVersions = surefireVersions
    }

    Collection<TestExecution> executions() {
        [javaVersions, mavenVersions, surefireVersions]
            .combinations({ jdk, maven, surefire -> new TestExecution(jdk, maven, surefire) })
    }

    static class TestExecution {
        final JDK javaVersion
        final MavenDistribution maven
        final String surefireVersion

        TestExecution(JDK javaVersion, MavenDistribution maven, String surefireVersion) {
            this.javaVersion = javaVersion
            this.maven = maven
            this.surefireVersion = surefireVersion
        }


        @Override
        String toString() {
            return "TestExecution{" +
                "javaVersion=" + javaVersion +
                ", mavenDistribution=" + maven +
                ", surefireVersion='" + surefireVersion + '\'' +
                '}';
        }
    }
}
