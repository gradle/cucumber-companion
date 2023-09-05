package org.gradle.cucumber.companion

import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

class TestContextExtension implements IGlobalExtension {
    @Override
    void visitSpec(SpecInfo spec) {
        spec.addInterceptor {
            spec.allFeatures.forEach { feature ->
                if (feature.parameterized) {
                    def currentNameProvider = feature.iterationNameProvider
                    feature.iterationNameProvider = {
                        def defaultName = currentNameProvider == null ? feature.name : currentNameProvider.getName(it)
                        "$defaultName ${TestContext.asName()}".toString()
                    }
                } else {
                    feature.displayName += " ${TestContext.asName()}"
                }
            }
            it.proceed()
        }
    }
}
