package org.gradle.cucumber.companion.plugintesting

import org.gradle.api.NamedDomainObjectContainer

abstract class MavenDistributionExtension {

    abstract val versions: NamedDomainObjectContainer<MavenDistribution>
}
