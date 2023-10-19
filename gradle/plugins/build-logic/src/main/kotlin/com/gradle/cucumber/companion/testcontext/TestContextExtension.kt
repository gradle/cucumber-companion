package com.gradle.cucumber.companion.testcontext

import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.process.CommandLineArgumentProvider

abstract class TestContextExtension : CommandLineArgumentProvider {
    companion object {
        const val NAME = "testContext"
    }

    @get:Input
    abstract val mappings: MapProperty<String, String>

    override fun asArguments(): List<String> {
        return mappings.orElse(mapOf()).get()
            .map { "-DtestContext.internal.${it.key}=${it.value}" }.toList()
    }
}
