package com.crossbowffs.quotelock

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

internal fun Project.configureApplicationPlugin() {
    pluginManager.apply {
        apply(libs.plugins.android.application.get().pluginId)
        apply(libs.plugins.android.kotlin.get().pluginId)
    }
}

internal fun ApplicationExtension.configureApplicationExtension() {
    configureKotlinAndroid()
    defaultConfig {
        applicationId = Configs.NAMESPACE
        targetSdk = Configs.TARGET_SDK_VERSION
    }
    namespace = Configs.NAMESPACE
}