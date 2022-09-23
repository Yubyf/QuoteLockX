import org.gradle.api.JavaVersion

object Configs {
    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33

    // See https://github.com/gradle/gradle/issues/18935
    const val jvmTarget = "1.8"

    val sourceCompatibility = JavaVersion.VERSION_1_8
    val targetCompatibility = JavaVersion.VERSION_1_8
}