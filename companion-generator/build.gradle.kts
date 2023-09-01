@file:Suppress("UnstableApiUsage")

plugins {
    java
    groovy
    `jvm-test-suite`
    `java-test-fixtures`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testFixturesApi(platform(libs.groovy.bom.get().toString()))
    testFixturesApi(libs.groovy.core)
    testFixturesApi(libs.groovy.nio)
    testFixturesApi(platform(libs.spock.bom.get().toString()))
    testFixturesApi(libs.spock.core)
}


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock(libs.versions.spock)
            dependencies {
                implementation(platform(libs.groovy.bom.get().toString()))
            }
        }
    }
}