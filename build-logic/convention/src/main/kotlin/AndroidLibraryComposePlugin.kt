import com.android.build.gradle.LibraryExtension
import com.crossbowffs.quotelock.configureAndroidCompose
import com.crossbowffs.quotelock.configureComposeExtension
import com.crossbowffs.quotelock.configureLibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureLibraryPlugin()

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this@configure)
            }

            configureComposeExtension()
        }
    }
}