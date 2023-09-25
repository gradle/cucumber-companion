import org.gradle.api.internal.artifacts.transform.UnzipTransform
import org.gradle.cucumber.companion.plugintesting.MavenDistribution
import org.gradle.cucumber.companion.plugintesting.MavenDistributionExtension

extensions.create<MavenDistributionExtension>("mavenDistributions").apply {
    downloadAndExtractMavenDistro(versions)
}

fun downloadAndExtractMavenDistro(
    container: NamedDomainObjectContainer<out MavenDistribution>
) {
    container.all {
        val safeName = name.replace('.', '_')
        val configuration = configurations.create("mavenRelease_${safeName}") {
            isCanBeResolved = true
            isCanBeConsumed = false
            resolutionStrategy.cacheDynamicVersionsFor(1, TimeUnit.HOURS)
            attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
        }
        dependencies {
            registerTransform(UnzipTransform::class) {
                from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.ZIP_TYPE)
                to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
            }
            configuration("org.apache.maven:apache-maven:${name}") {
                artifact {
                    classifier = "bin"
                    type = "zip"
                    isTransitive = false
                }
            }
        }
        installationDirectory.convention(layout.dir(provider { configuration.singleFile.listFiles()?.single() }))
        resolvedVersion.convention(
            provider {
                configuration.resolvedConfiguration.firstLevelModuleDependencies.single().moduleVersion
            }
        )
    }
}
