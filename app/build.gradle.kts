@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.crossbowffs.quotelock.Configs

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.quotelockx.android.application)
    alias(libs.plugins.quotelockx.android.compose.application)
    alias(libs.plugins.quotelockx.android.jacoco.application)

    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

//region Keystore
var keystoreFilepath: String? = null
var keystoreStorePassword: String? = null
var keystoreAlias: String? = null
var keystorePassword: String? = null
// Load the local keystore file first. Configure variables in local.properties.
keystoreFilepath = gradleLocalProperties(rootDir, providers).let { properties ->
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
        versionCode = 29
        versionName = "3.2.1"

        testInstrumentationRunner = "com.crossbowffs.quotelock.CustomTestRunner"

        buildConfigField("int", "MODULE_VERSION", "4")
        buildConfigField("String", "LOG_TAG", "\"QuoteLockX\"")

        resValue("string", "account_type", "${applicationId}.account")
        resValue("string", "account_authority", "${applicationId}.collection.provider")
        resourceConfigurations += arrayOf("en", "zh-rCN", "zh-rTW")
    }

    buildTypes {
        debug {
            buildConfigField("int", "LOG_LEVEL", "4")
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

        applicationVariants.all {
            outputs.map { it as BaseVariantOutputImpl }.forEach { output ->
                output.outputFileName = Configs.generatePackageName(
                    versionName,
                    buildType.name,
                    rootDir
                )
            }
        }
    }

    packaging {
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
}

dependencies {
    // Xposed
    compileOnly(libs.xposed)

    // AndroidX
    implementation(libs.bundles.androidx.standard)

    // Google
    implementation(libs.bundles.google) {
        exclude(group = "org.apache.httpcomponents")
    }

    // Room
    implementation(libs.bundles.androidx.room)
    ksp(libs.androidx.room.compiler)

    // Koin
    implementation(libs.bundles.koin)
    ksp(libs.koin.ksp.compiler)

    // Jetpack Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.bundles.compose.standard)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.coil)
    implementation(libs.bundles.ktor)

    implementation(libs.markwon.core)

    // Accompanist
    implementation(libs.bundles.accompanist)

    implementation(libs.remote.preferences)
    implementation(libs.jsoup)
    implementation(libs.datastore.preferences)
    implementation(libs.truetype.parser.light)
    implementation(libs.open.csv) {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.bundles.koin.test)
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.bundles.compose.test)
    androidTestImplementation(libs.koin.android.test)
    debugImplementation(libs.compose.test.manifest)
}