package com.crossbowffs.quotelock

import org.gradle.api.Project

internal fun Project.configureRoomPlugin() {
    pluginManager.apply {
        apply(libs.plugins.room.get().pluginId)
        apply(libs.plugins.ksp.get().pluginId)
    }
}
