import com.android.build.api.dsl.ApplicationExtension
import com.crossbowffs.quotelock.configureApplicationExtension
import com.crossbowffs.quotelock.configureApplicationPlugin
import com.crossbowffs.quotelock.configureKotlin
import com.crossbowffs.quotelock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureApplicationPlugin()
            extensions.configure<ApplicationExtension> {
                configureApplicationExtension()

                compileOptions {
                    // Flag to enable support for the new language APIs
                    isCoreLibraryDesugaringEnabled = true
                }

                packaging {
                    resources {
                        excludes += "/META-INF/INDEX.LIST"
                        excludes += "/META-INF/NOTICE.md"
                        excludes += "/META-INF/LICENSE.md"
                    }
                }

                lint {
                    abortOnError = false
                }
            }
            configureKotlin()

            dependencies {
                "coreLibraryDesugaring"(libs.desugar.jdk)
            }
        }
    }
}