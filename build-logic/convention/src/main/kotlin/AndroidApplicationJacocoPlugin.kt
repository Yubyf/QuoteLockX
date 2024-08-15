import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.crossbowffs.quotelock.configureManualJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationJacocoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")
            extensions.configure<BaseAppModuleExtension> {
                buildTypes.configureEach {
                    enableAndroidTestCoverage = true
                    enableUnitTestCoverage = true
                }
            }

            configureManualJacoco(extensions.getByType<ApplicationAndroidComponentsExtension>())
        }
    }
}
