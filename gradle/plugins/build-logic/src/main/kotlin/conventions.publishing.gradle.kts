plugins {
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow")
}

publishing {
    publications {
        withType(MavenPublication::class.java) {
            signing.sign(this)

            pom {
                name.set(provider {
                    artifactId.split("-").joinToString(" ") { it.replaceFirstChar(Character::toUpperCase) }
                } )
                description.set(provider { project.description })

                url.set("https://github.com/gradle/cucumber-companion/")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        name.set("The Gradle team")
                        organization.set("Gradle Inc.")
                        organizationUrl.set("https://gradle.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/gradle/cucumber-companion.git")
                    developerConnection.set("scm:git:ssh://git@github.com:gradle/cucumber-companion.git")
                    url.set("https://github.com/gradle/cucumber-companion/")
                }
            }
        }
    }
}

tasks.withType<Sign>().configureEach {
    onlyIf("Run on TeamCity") { System.getenv("TEAMCITY_VERSION") != null }
}

signing {
    useInMemoryPgpKeys(System.getenv("PGP_SIGNING_KEY"), System.getenv("PGP_SIGNING_KEY_PASSPHRASE"))
}
