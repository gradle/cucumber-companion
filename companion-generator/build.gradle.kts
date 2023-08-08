@file:Suppress("UnstableApiUsage")

plugins {
    java
    groovy
    `jvm-test-suite`
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
