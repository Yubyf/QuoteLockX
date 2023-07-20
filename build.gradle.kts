// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin).apply(false)
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.kapt).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.android.hilt).apply(false)
    alias(libs.plugins.gradle.versions.plugin)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
