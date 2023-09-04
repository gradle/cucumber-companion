import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.maven.MavenPublication

abstract class MavenPluginTestingExtension {

    companion object {
        const val TEST_REPOSITORY_NAME = "TestLocal"
    }

    /**
     * Maven versions to test against
     */
    abstract val mavenVersions: SetProperty<String>

    /**
     * The publication for the maven plugin under test
     */
    abstract val pluginPublication: Property<MavenPublication>

    fun publishToTestRepositoryTaskName(): Provider<String> {
        return pluginPublication.map { it.name }
            .map { it.replaceFirstChar(Char::titlecase) }
            .map { "publish${it}PublicationTo${TEST_REPOSITORY_NAME}Repository" }
    }
}
