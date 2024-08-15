import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.crossbowffs.quotelock.configureManualJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryJacocoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")
            extensions.configure<LibraryExtension> {
                buildTypes.configureEach {
                    enableAndroidTestCoverage = true
                    enableUnitTestCoverage = true
                }
            }

            configureManualJacoco(extensions.getByType<LibraryAndroidComponentsExtension>())
        }
    }
}
