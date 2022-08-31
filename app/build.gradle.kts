import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

//region Keystore
var keystoreFilepath: String? = null
var keystoreStorePassword: String? = null
var keystoreAlias: String? = null
var keystorePassword: String? = null
// Load the local keystore file first. Configure variables in local.properties.
keystoreFilepath = gradleLocalProperties(rootDir).let { properties ->
    (properties["keystore.path"] as String?)?.let { path ->
        rootDir.absolutePath + File.separatorChar + path
    }?.also {
        keystoreStorePassword = properties["keystore.store_password"] as String?
        keystoreAlias = properties["keystore.alias"] as String?
        keystorePassword = properties["keystore.password"] as String?
    } ?: System.getenv("SIGNING_KEYSTORE_PATH")?.also {
        // If the local keystore does not exist, try to read keystore variables in the Github workflow.
        keystoreStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
        keystoreAlias = System.getenv("SIGNING_KEY_ALIAS")
        keystorePassword = System.getenv("SIGNING_KEY_PASSWORD")
    }
}
//endregion

android {
    compileSdk = 33

    keystoreFilepath?.let { keystore ->
        signingConfigs {
            create("release") {
                storeFile = file(keystore)
                storePassword = keystoreStorePassword
                keyAlias = keystoreAlias
                keyPassword = keystorePassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.yubyf.quotelockx"
        versionCode = 22
        versionName = "2.2.1"
        minSdk = 21

        targetSdk = 33

        buildConfigField("int", "MODULE_VERSION", "3")
        buildConfigField("int", "CUSTOM_QUOTES_DB_VERSION", "4")
        buildConfigField("int", "QUOTE_COLLECTIONS_DB_VERSION", "4")
        buildConfigField("int", "QUOTE_HISTORIES_DB_VERSION", "4")
        buildConfigField("int", "FORTUNE_QUOTES_DB_VERSION", "4")
        buildConfigField("String", "LOG_TAG", "\"QuoteLockX\"")

        resValue("string", "account_type", "${applicationId}.account")
        resValue("string", "account_authority", "${applicationId}.collection.provider")

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/build/schemas")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("int", "LOG_LEVEL", "2")
            buildConfigField("boolean", "LOG_TO_XPOSED", "false")
        }

        release {
            runCatching {
                signingConfig = signingConfigs.getByName("release")
            }.onFailure {
                logger.error("Failed to set signing config: ${it.message}")
            }
            // Enables code shrinking.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            buildConfigField("int", "LOG_LEVEL", "4")
            buildConfigField("boolean", "LOG_TO_XPOSED", "true")

            applicationVariants.all {
                outputs.map { it as BaseVariantOutputImpl }.forEach { output ->
                    output.outputFileName =
                        "QuoteLockX-v$versionName${if (signingConfig == null) "-unsigned" else ""}.apk"
                }
            }
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources.excludes.run {
            add("META-INF/DEPENDENCIES")
            add("META-INF/LICENSE")
            add("META-INF/LICENSE.txt")
            add("META-INF/license.txt")
            add("META-INF/NOTICE")
            add("META-INF/NOTICE.txt")
            add("META-INF/notice.txt")
            add("META-INF/ASL2.0")
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
}

dependencies {
    implementation("com.crossbowffs.remotepreferences:remotepreferences:0.9")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("io.github.yubyf.datastorepreferences:datastorepreferences:1.2.1")
    implementation("io.github.yubyf:truetypeparser-light:2.0.1")
    implementation("com.google.android.material:material:1.6.1")
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")

    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    // Fix the conflict between concurrent-futures and guava libs.
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.api-client:google-api-client-android:1.33.2") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-drive:v3-rev20220214-1.32.1") {
        exclude(group = "org.apache.httpcomponents")
    }

    // Room
    val roomVersion = "2.4.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Hilt
    val hiltVersion = "2.42"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Jetpack Compose
    val navVersion = "2.5.1"
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.compose.material3:material3:1.0.0-beta01")
    implementation("androidx.compose.animation:animation:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.1")
    implementation("androidx.compose.compiler:compiler:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")
    implementation("io.coil-kt:coil-compose:2.2.0")

    // Accompanist
    val accompanistVersion = "0.26.2-beta"
    implementation("com.google.accompanist:accompanist-navigation-animation:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")

    implementation("com.opencsv:opencsv:5.6") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}