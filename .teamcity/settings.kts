import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.05"
project {
    description = "Publishes the Cucumber Companion Plugins to Maven Central or Artifactory"

    subProject(publish(
        "Publish to Artifactory",
        "Publishes the Cucumber Companion Plugins to Artifactory",
        "publishAllPublicationsToArtifactoryRepository"
    ) {
        it.param("env.ORG_GRADLE_PROJECT_artifactoryUsername", "%artifactoryUsername%")
        it.password("env.ORG_GRADLE_PROJECT_artifactoryPassword", "%artifactoryPassword%")
    })

    subProject(publish(
        "Publish to Maven Central",
        "Publishes the Cucumber Companion Plugins to Maven Central",
        "publishAllPublicationsToSonatypeRepository"
    ) {
        it.param("env.ORG_GRADLE_PROJECT_sonatypeUsername", "%mavenCentralStagingRepoUser%")
        it.password("env.ORG_GRADLE_PROJECT_sonatypePassword", "%mavenCentralStagingRepoPassword%")
    })

    params {
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
        param("env.GRADLE_CACHE_REMOTE_URL", "%gradle.cache.remote.url%")
        param("env.GRADLE_CACHE_REMOTE_USERNAME", "%gradle.cache.remote.username%")
        password("env.GRADLE_CACHE_REMOTE_PASSWORD", "%gradle.cache.remote.password%")
    }
}

fun publish(
    name: String,
    description: String,
    gradlePublishTaskName: String,
    extraParams: (p: ParametrizedWithType) -> Unit
): Project {
    return Project {
        this.name = name
        this.id = RelativeId(name.toId())
        this.description = description

        buildType {
            this.name = name
            this.id = RelativeId(this.name.toId())

            vcs {
                root(DslContext.settingsRoot)
                checkoutMode = CheckoutMode.ON_AGENT
                cleanCheckout = true
            }

            requirements {
                contains("teamcity.agent.jvm.os.name", "Linux")
            }

            steps {
                gradle {
                    useGradleWrapper = true
                    tasks = "clean $gradlePublishTaskName"
                    gradleParams = "--build-cache --no-configuration-cache"
                }
            }
            params {
                extraParams(this)
                password("env.PGP_SIGNING_KEY", "%pgpSigningKey%")
                password("env.PGP_SIGNING_KEY_PASSPHRASE", "%pgpSigningPassphrase%")
            }
        }
    }
}
