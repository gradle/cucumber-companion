import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.CommandLineArgumentProvider

abstract class MavenDistribution(@get:Internal val name: String) : CommandLineArgumentProvider {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val installationDirectory: DirectoryProperty

    @get:Input
    abstract val resolvedVersion: Property<String>

    override fun asArguments() = listOf(
        "-DtestContext.internal.mavenHome.${resolvedVersion.get()}=${installationDirectory.asFile.get().absolutePath}"
    )
}
