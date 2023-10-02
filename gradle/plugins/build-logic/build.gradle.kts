plugins {
    kotlin("jvm") version libs.versions.kotlin
    `kotlin-dsl`
    `groovy`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    // hack to make the version catalog available to convention plugin scripts (https://github.com/gradle/gradle/issues/17968)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.shadowPlugin)
    implementation((libs.licensePlugin))
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(project())
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
            }
        }
    }
}
gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])


