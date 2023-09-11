val isCI = System.getenv("CI")?.toBoolean() ?: false
val isCC = project.gradle.startParameter.isConfigurationCacheRequested

require(!isCC || !isCI) { "Configuration-Cache should be disabled on CI" }
