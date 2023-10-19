import org.gradle.api.tasks.testing.Test
import com.gradle.cucumber.companion.testcontext.TestContextExtension

val testContextExtension = extensions.create<TestContextExtension>(TestContextExtension.NAME)

tasks.withType(Test::class.java).configureEach {
    this.jvmArgumentProviders += testContextExtension
}
