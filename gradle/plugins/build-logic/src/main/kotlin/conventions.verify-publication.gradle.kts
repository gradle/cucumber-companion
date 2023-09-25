import org.gradle.cucumber.companion.verifypublication.VerifyPublicationExtension
import org.gradle.cucumber.companion.verifypublication.VerifyPublicationTask

plugins {
    `maven-publish`
}

val extension = extensions.create<VerifyPublicationExtension>(VerifyPublicationExtension.NAME)
extension.verificationRepoDir.convention(layout.buildDirectory.dir("verifyPublication/repository"))

publishing {
    repositories {
        maven {
            name = "verifyPublication"
            url = extension.verificationRepoDir.get().asFile.toURI()
        }
    }
}

val publishToVerificationRepoTasks = tasks.withType<PublishToMavenRepository>().matching { it.name.endsWith("VerifyPublicationRepository") }
publishToVerificationRepoTasks.configureEach {
    outputs.dir(extension.verificationRepoDir.locationOnly)
    mustRunAfter(clearTempRepo)
}

val clearTempRepo by tasks.creating(Delete::class.java) {
    delete(extension.verificationRepoDir)
}

val verifyPublication by tasks.creating(VerifyPublicationTask::class.java) {
    group = "verification"
    description = "Verifies structure and contents of all published artifacts"
    dependsOn(clearTempRepo, publishToVerificationRepoTasks)

    version = providers.gradleProperty("version")
    groupId = providers.gradleProperty("group")
    verifyPublicationExtension = provider { extension }
    //doLast {
    //verifyPublicationExtension.verify(project.version.toString(), project.group.toString()) { project.zipTree(it) }
    /*
            val expectedLocation = project.group.toString().replace(".", File.separator)
            val v = project.version.toString()
            val foo = verifyPublicationExtension.verificationRepoDir.map { it.dir(expectedLocation) }.get()
            println("Checking published artifacts in ${foo}")
            require(foo.asFile.list().size == 3) { "Published more artifacts than expected" }
            require(foo.dir("cucumber-companion-gradle-plugin").asFile.exists()) { "Did not publish gradle plugin artifact(s)" }
            require(foo.dir("org.gradle.cucumber.companion.gradle.plugin").asFile.exists()) { "Did not publish gradle plugin marker file" }

            require(foo.dir("cucumber-companion-maven-plugin").asFile.exists()) { "Did not publish maven plugin artifact(s)" }
            val jar =
                zipTree(foo.dir("cucumber-companion-maven-plugin/${v}/cucumber-companion-maven-plugin-$v.jar"))
            require(!jar.matching { include("META-INF/maven/plugin.xml") }.isEmpty) { "Maven plugin jar lacks maven plugin descriptor" }
            require(!jar.matching { include("META-INF/maven/org.gradle.cucumber.companion/maven-plugin/plugin-help.xml") }.isEmpty) { "Maven plugin jar lacks maven help descriptor" }
            require(
                jar.matching { include("META-INF/maven/org.gradle.cucumber.companion/maven-plugin/plugin-help.xml") }.singleFile.readText()
                    .contains("<artifactId>cucumber-companion-maven-plugin</artifactId>")
            ) { "Maven plugin jar lacks maven help descriptor" }*/
    //}
}
