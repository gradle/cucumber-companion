@file:Suppress("UnstableApiUsage")

plugins {
    java
    groovy
    `jvm-test-suite`
    alias(libs.plugins.mavenPluginDevelopment)
}


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock()
            dependencies {
                implementation(libs.groovy)
            }
        }
    }
}
