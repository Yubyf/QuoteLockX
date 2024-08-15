package com.crossbowffs.quotelock

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: CommonExtension<*, *, *, *, *, *>) {
    pluginManager.apply {
        apply(libs.plugins.compose.compiler.get().pluginId)
    }
    configureAndroidComposeExtension(extension)

    dependencies {
        val composeBom = platform(libs.compose.bom)
        "implementation"(composeBom)
        "androidTestImplementation"(composeBom)
    }
}

private fun Project.configureAndroidComposeExtension(extension: CommonExtension<*, *, *, *, *, *>) {
    with(extension) {
        buildFeatures {
            compose = true
        }
    }
}