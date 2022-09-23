import Versions.accompanistVersion
import Versions.composeVersion
import Versions.hiltVersion
import Versions.materialIconsVersion
import Versions.navVersion
import Versions.roomVersion

object Versions {
    const val androidGradlePluginVersion = "7.2.2"
    const val kotlinVersion = "1.7.10"
    const val hiltVersion = "2.44"
    const val gradleVersionsPluginVersion = "0.42.0"

    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33
    const val jvmTarget = "11"

    internal const val roomVersion = "2.4.2"
    const val composeVersion = "1.3.1"
    internal const val accompanistVersion = "0.26.4-beta"

    internal const val navVersion = "2.5.1"
    internal const val materialIconsVersion = "1.2.1"
}

object Dependencies {

    object AndroidX {
        const val activity = "androidx.activity:activity:1.6.0"
        const val workRuntimeKtx = "androidx.work:work-runtime-ktx:2.7.1"
        const val concurrentFutures = "androidx.concurrent:concurrent-futures:1.1.0"

        // Fix the conflict between concurrent-futures and guava libs.
        const val listenableFuture =
            "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava"
    }

    object Room {
        const val runtime = "androidx.room:room-runtime:$roomVersion"
        const val compiler = "androidx.room:room-compiler:$roomVersion"
        const val ktx = "androidx.room:room-ktx:$roomVersion"
    }

    object Hilt {
        const val android = "com.google.dagger:hilt-android:$hiltVersion"
        const val compiler = "com.google.dagger:hilt-compiler:$hiltVersion"
        const val navigationCompose = "androidx.hilt:hilt-navigation-compose:1.0.0"
    }

    object Google {
        const val material = "com.google.android.material:material:1.6.1"
        const val playServicesAuth = "com.google.android.gms:play-services-auth:20.3.0"
        const val apiClient = "com.google.api-client:google-api-client-android:2.0.0"
        const val apiServicesDrive =
            "com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0"
    }

    object Compose {
        const val compiler = "androidx.compose.compiler:compiler:$composeVersion"
        const val activity = "androidx.activity:activity-compose:1.6.0"
        const val material3 = "androidx.compose.material3:material3:1.0.0-beta03"
        const val animation = "androidx.compose.animation:animation:1.2.1"
        const val uiTooling = "androidx.compose.ui:ui-tooling:1.2.1"
        const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:1.2.1"
        const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1"
        const val navigation = "androidx.navigation:navigation-compose:$navVersion"
        const val animationGraphics =
            "androidx.compose.animation:animation-graphics:1.3.0-beta03"

        // Material design icons
        const val materialIconCore =
            "androidx.compose.material:material-icons-core:$materialIconsVersion"
        const val materialIconExtended =
            "androidx.compose.material:material-icons-extended:$materialIconsVersion"

        const val coil = "io.coil-kt:coil-compose:2.2.1"
    }

    object Accompanist {
        const val navigation =
            "com.google.accompanist:accompanist-navigation-animation:$accompanistVersion"
        const val systemUiController =
            "com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion"
        const val pager = "com.google.accompanist:accompanist-pager:$accompanistVersion"
        const val pagerIndicators =
            "com.google.accompanist:accompanist-pager-indicators:$accompanistVersion"
    }

    object Xposed {
        const val api = "de.robv.android.xposed:api:82"
        const val apiSource = "de.robv.android.xposed:api:82:sources"
    }

    const val remotePreferences = "com.crossbowffs.remotepreferences:remotepreferences:0.9"
    const val jsoup = "org.jsoup:jsoup:1.15.3"
    const val datastorePreferences =
        "io.github.yubyf.datastorepreferences:datastorepreferences:1.2.2"
    const val trueTypeParserLight = "io.github.yubyf:truetypeparser-light:2.1.3"
    const val openCsv = "com.opencsv:opencsv:5.6"
}