plugins {
    alias(libs.plugins.nexusPublish)
}

nexusPublishing {
    packageGroup.set("org.gradle")
    this.repositories {
        sonatype {
            // https://central.sonatype.org/news/20210223_new-users-on-s01/
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
        create("artifactory") {
            nexusUrl.set(uri("https://repo.gradle.org/artifactory/enterprise-libs-release-candidates/"))
        }
    }
}
