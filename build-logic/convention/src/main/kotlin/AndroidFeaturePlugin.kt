
import com.crossbowffs.quotelock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(libs.plugins.quotelockx.android.library.get().pluginId)
            }

            dependencies {
                "implementation"(project(":core:common"))
                "implementation"(project(":core:ui:design"))
                "implementation"(project(":core:ui:contract"))
                "implementation"(project(":core:ui:component"))
                "implementation"(project(":core:model"))
                "implementation"(project(":core:data"))

                "implementation"(libs.androidx.core.ktx)
                "implementation"(libs.androidx.appcompat)
                "implementation"(libs.androidx.lifecycle.viewmodel.ktx)
                "implementation"(libs.compose.navigation)
                "implementation"(libs.kotlinx.coroutines.android)

                "testImplementation"(kotlin("test"))
                "testImplementation"(libs.junit)
                "androidTestImplementation"(kotlin("test"))
                "androidTestImplementation"(libs.bundles.androidx.test)
            }
        }
    }
}