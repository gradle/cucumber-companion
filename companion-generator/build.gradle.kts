@file:Suppress("UnstableApiUsage")

plugins {
    java
    groovy
    `jvm-test-suite`
    `java-test-fixtures`
    id("conventions.code-style")
    id("conventions.java-toolchain")
}

dependencies {
    testFixturesApi(platform(libs.groovy.bom))
    testFixturesApi(libs.groovy.core)
    testFixturesApi(libs.groovy.nio)
    testFixturesApi(platform(libs.spock.bom))
    testFixturesApi(libs.spock.core)
    testFixturesImplementation(libs.jetbrains.annotations)
}


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock(libs.versions.spock)
            dependencies {
                implementation(platform(libs.groovy.bom))
            }
        }
    }
}
