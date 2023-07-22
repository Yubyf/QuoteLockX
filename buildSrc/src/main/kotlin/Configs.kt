import org.gradle.api.JavaVersion

object Configs {
    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33

    const val namespace = "com.yubyf.quotelockx"
    const val versionCode = 29
    const val versionName = "3.2.1"

    val javaVersion = JavaVersion.VERSION_11

    val JavaVersion.versionCode: Int
        get() = ordinal + 1
}
