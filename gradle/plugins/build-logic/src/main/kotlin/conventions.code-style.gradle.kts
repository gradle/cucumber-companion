import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import java.time.LocalDateTime
import java.time.Year

plugins {
    id("com.github.hierynomus.license")
    checkstyle
    codenarc
}

// This is just used to automatically add the license headers via ./gradlew licenseFormat
// Presence of headers is checked by checkStyle
license {
    header = rootProject.file("gradle/licenseHeader.txt")
    excludes(listOf("**/*.tokens", "META-INF/LICENSE", "META-INF/NOTICE.txt", "META-INF/licenses/**"))
    mapping(
        mapOf(
            "java" to "SLASHSTAR_STYLE",
            "groovy" to "SLASHSTAR_STYLE",
            "kt" to "SLASHSTAR_STYLE"
        )
    )

    ext.set("year", Year.now().value)
    sourceSets = project.sourceSets
}

listOf(LicenseCheck::class.java, LicenseFormat::class.java)
    .map { tasks.withType(it) }
    .map {
        it.configureEach {
            notCompatibleWithConfigurationCache("just not")
        }
    }

val codeStyle by tasks.creating(DefaultTask::class.java) {
    dependsOn(tasks.withType(Checkstyle::class.java))
    dependsOn(tasks.withType(CodeNarc::class.java))
}

tasks.named("check").configure { dependsOn(codeStyle) }
