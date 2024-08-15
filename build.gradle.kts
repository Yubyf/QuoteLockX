import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin).apply(false)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.room) apply false
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.gradle.versions.plugin)
}

configureVersionsUpdates()

// region Versions
/**
 * Configure the [DependencyUpdatesTask].
 *
 * **Note:** This task is not available when the configure cache
 * is enabled in [gradle.properties](./gradle.properties).
 * Toggle the `org.gradle.configuration-cache` off to run this task.
 *
 * See [repo](https://github.com/ben-manes/gradle-versions-plugin)
 */
fun Project.configureVersionsUpdates() {
    /**
     * Rejects non-stable versions.
     */
    fun isNonStable(version: String): Boolean {
        val stableKeyword =
            listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    tasks.withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
        outputFormatter = "html"
    }
}
// endregion