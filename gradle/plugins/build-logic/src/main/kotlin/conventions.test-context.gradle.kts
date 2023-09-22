import org.gradle.api.tasks.testing.Test

val testContextExtension = extensions.create<TestContextExtension>(TestContextExtension.NAME)

tasks.withType(Test::class.java).configureEach {
    this.jvmArgumentProviders += testContextExtension
}
