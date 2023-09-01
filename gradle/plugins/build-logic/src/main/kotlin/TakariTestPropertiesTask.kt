import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.*
import java.util.*

/**
 * Generates the test.properties file that is required for executing maven plugin tests via takari
 */
abstract class TakariTestPropertiesTask : DefaultTask() {

    @get:Input
    abstract val groupId: Property<String>

    @get:Input
    abstract val artifactId: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:[InputDirectory PathSensitive(PathSensitivity.NAME_ONLY)]
    abstract val testRepositoryPath: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generateTestProperties() {
        val testProperties = Properties()
        testProperties.putAll(
            mapOf(
                "project.groupId" to groupId.get(),
                "project.artifactId" to artifactId.get(),
                "project.version" to version.get(),
                "localRepository" to testRepositoryPath.get().dir("repository").asFile.path,
                "repository.0" to "<id>central</id><url>https://repo.maven.apache.org/maven2</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots>",
                "updateSnapshots" to "false"
            )
        )
        outputDirectory.get().file("test.properties").asFile.writer().use { testProperties.store(it, "") }
    }
}
