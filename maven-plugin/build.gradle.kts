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
            useSpock(libs.versions.spock)
            dependencies {
                implementation(platform(libs.groovy.bom.get().toString()))
            }
        }
    }
}

listOf("generateMavenPluginDescriptor", "generateMavenPluginHelpMojoSources").forEach {
    tasks.named(it) {
        notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }
}
