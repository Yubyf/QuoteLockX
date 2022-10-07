import Dependencies.Accompanist
import Dependencies.AndroidX
import Dependencies.Compose
import Dependencies.Google
import Dependencies.Hilt
import Dependencies.Room
import Dependencies.Test
import Dependencies.Xposed
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
    compileSdk = Configs.compileSdk

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
        versionCode = 25
        versionName = "3.0.2"
        minSdk = Configs.minSdk

        targetSdk = Configs.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
        sourceCompatibility = Configs.sourceCompatibility
        targetCompatibility = Configs.targetCompatibility
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
        jvmTarget = Configs.jvmTarget
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
}

dependencies {
    // Xposed
    compileOnly(Xposed.api)

    // AndroidX
    implementation(AndroidX.activity)
    implementation(AndroidX.workRuntimeKtx)
    implementation(AndroidX.concurrentFutures)
    // Fix the conflict between concurrent-futures and guava libs.
    implementation(AndroidX.listenableFuture)

    // Google
    implementation(Google.material)
    implementation(Google.playServicesAuth)
    implementation(Google.apiClient) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(Google.apiServicesDrive) {
        exclude(group = "org.apache.httpcomponents")
    }

    // Room
    implementation(Room.runtime)
    kapt(Room.compiler)
    implementation(Room.ktx)

    // Hilt
    implementation(Hilt.android)
    kapt(Hilt.compiler)
    implementation(Hilt.navigationCompose)

    // Jetpack Compose
    implementation(Compose.compiler)
    implementation(Compose.activity)
    implementation(Compose.material3)
    debugImplementation(Compose.uiTooling)
    implementation(Compose.uiToolingPreview)
    implementation(Compose.lifecycleViewModel)
    implementation(Compose.navigation)
    implementation(Compose.animation)
    implementation(Compose.animationGraphics)
    // Material design icons
    implementation(Compose.materialIconCore)
    implementation(Compose.materialIconExtended)
    implementation(Compose.coil)

    // Accompanist
    implementation(Accompanist.navigation)
    implementation(Accompanist.systemUiController)
    implementation(Accompanist.pager)
    implementation(Accompanist.pagerIndicators)

    implementation(Dependencies.remotePreferences)
    implementation(Dependencies.jsoup)
    implementation(Dependencies.datastorePreferences)
    implementation(Dependencies.trueTypeParserLight)
    implementation(Dependencies.openCsv) {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // Test
    androidTestImplementation(Test.core)
    androidTestImplementation(Test.rules)
    androidTestImplementation(Compose.test)
    androidTestImplementation(Compose.uiTest)
    debugImplementation(Compose.testManifest)
}