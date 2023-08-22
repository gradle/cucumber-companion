@file:Suppress("UnstableApiUsage")

plugins {
    java
    groovy
    `jvm-test-suite`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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
