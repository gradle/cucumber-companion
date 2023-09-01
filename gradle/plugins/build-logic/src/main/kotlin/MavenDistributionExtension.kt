import org.gradle.api.NamedDomainObjectContainer

abstract class MavenDistributionExtension {

    abstract val versions: NamedDomainObjectContainer<MavenDistribution>
}
