package com.crossbowffs.quotelock

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

internal fun Project.configureLibraryPlugin() {
    pluginManager.apply {
        apply(libs.plugins.android.library.get().pluginId)
        apply(libs.plugins.android.kotlin.get().pluginId)
    }
}

internal fun LibraryExtension.configureLibraryExtension() {
    configureKotlinAndroid()
}