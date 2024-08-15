import androidx.room.gradle.RoomExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.crossbowffs.quotelock.configureRoomPlugin
import com.crossbowffs.quotelock.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class AndroidRoomPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureRoomPlugin()

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            if (extensions.findByType(ApplicationExtension::class) != null) {
                configure<ApplicationExtension> {
                    sourceSets {
                        getByName("androidTest").assets.srcDir("$projectDir/schemas")
                    }
                }
            } else if (extensions.findByType(LibraryExtension::class) != null) {
                configure<LibraryExtension> {
                    sourceSets {
                        getByName("androidTest").assets.srcDir("$projectDir/schemas")
                    }
                }
            }

            dependencies {
                "implementation"(libs.bundles.androidx.room)
                "ksp"(libs.androidx.room.compiler)
            }
        }
    }
}