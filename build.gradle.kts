// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version(Versions.androidGradlePluginVersion).apply(false)
    id("org.jetbrains.kotlin.android").version(Versions.kotlinVersion).apply(false)
    id("org.jetbrains.kotlin.kapt").version(Versions.kotlinVersion).apply(false)
    id("com.google.dagger.hilt.android").version(Versions.hiltVersion).apply(false)
    id("com.github.ben-manes.versions").version(Versions.gradleVersionsPluginVersion)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
