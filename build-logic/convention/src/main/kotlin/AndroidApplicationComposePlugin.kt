import com.android.build.api.dsl.ApplicationExtension
import com.crossbowffs.quotelock.configureAndroidCompose
import com.crossbowffs.quotelock.configureApplicationPlugin
import com.crossbowffs.quotelock.configureComposeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureApplicationPlugin()

            extensions.configure<ApplicationExtension> {
                configureAndroidCompose(this@configure)
            }

            configureComposeExtension()
        }
    }
}