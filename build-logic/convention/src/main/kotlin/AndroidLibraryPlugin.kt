import com.android.build.gradle.LibraryExtension
import com.crossbowffs.quotelock.configureKotlin
import com.crossbowffs.quotelock.configureLibraryExtension
import com.crossbowffs.quotelock.configureLibraryPlugin
import com.crossbowffs.quotelock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureLibraryPlugin()
            extensions.configure<LibraryExtension> {
                configureLibraryExtension()

                packaging {
                    resources {
                        excludes += "/META-INF/INDEX.LIST"
                        excludes += "/META-INF/NOTICE.md"
                        excludes += "/META-INF/LICENSE.md"
                    }
                }
            }
            configureKotlin()
            dependencies {
                "testImplementation"(kotlin("test"))
                "testImplementation"(libs.junit)
                "androidTestImplementation"(kotlin("test"))
                "androidTestImplementation"(libs.bundles.androidx.test)
            }
        }
    }
}