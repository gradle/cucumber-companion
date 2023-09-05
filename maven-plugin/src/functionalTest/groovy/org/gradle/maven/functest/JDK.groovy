package org.gradle.maven.functest

import java.util.stream.Collectors
import java.util.stream.Stream

class JDK {

    static List<JDK> allJDKs() {
        def current = Stream.of(current())
        def others = System.getenv().entrySet().stream()
            .filter { it.key.startsWith("JDK") }
            .map { new File(it.value) }
            .map { new JDK(it) }

        return Stream.concat(current, others)
            .distinct()
            .collect(Collectors.toList())
    }

    static List<JDK> ifAvailable(int ... majorVersions) {
        return Arrays.stream(majorVersions)
            .mapToObj { "JDK$it" }
            .map { System.getenv(it) }
            .filter { it != null }
            .map { new File(it) }
            .map { new JDK(it) }
            .distinct()
            .collect(Collectors.toList())
    }

    static JDK current() {
        return new JDK(new File(System.getProperty("java.home")))
    }

    private final File javaHome

    JDK(File javaHome) {
        this.javaHome = javaHome
    }

    String getVersion() {
        def i = name.lastIndexOf('-')
        return name.substring(i + 1)
    }

    String getName() {
        return javaHome.name
    }

    File getJavaHome() {
        return javaHome
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (o == null || getClass() != o.class) return false
        JDK jdk = (JDK) o
        if (javaHome != jdk.javaHome) return false
        return true
    }

    int hashCode() {
        return (javaHome != null ? javaHome.hashCode() : 0)
    }

    @Override
    String toString() {
        return "jdk-" + version
    }
}
