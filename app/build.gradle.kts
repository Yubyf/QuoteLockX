@file:Suppress("UnstableApiUsage")

import Configs.versionCode
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kapt)
    alias(libs.plugins.android.hilt)
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
        applicationId = Configs.namespace
        versionCode = Configs.versionCode
        versionName = Configs.versionName
        minSdk = Configs.minSdk

        targetSdk = Configs.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("int", "MODULE_VERSION", "4")
        buildConfigField("int", "CUSTOM_QUOTES_DB_VERSION", "5")
        buildConfigField("int", "QUOTE_COLLECTIONS_DB_VERSION", "5")
        buildConfigField("int", "QUOTE_HISTORIES_DB_VERSION", "5")
        buildConfigField("int", "FORTUNE_QUOTES_DB_VERSION", "5")
        buildConfigField("String", "LOG_TAG", "\"QuoteLockX\"")

        resValue("string", "account_type", "${applicationId}.account")
        resValue("string", "account_authority", "${applicationId}.collection.provider")
        resourceConfigurations += arrayOf("en", "zh-rCN", "zh-rTW")

        javaCompileOptions {
            annotationProcessorOptions {
                compilerArgumentProviders(
                    RoomSchemaArgProvider(File(projectDir, "schemas"))
                )
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
        sourceCompatibility = Configs.javaVersion
        targetCompatibility = Configs.javaVersion
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
    kotlinOptions {
        jvmTarget = Configs.javaVersion.toString()
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.version.get()
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
    namespace = Configs.namespace
}

kotlin {
    jvmToolchain(Configs.javaVersion.versionCode)
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
    kapt(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.bundles.hilt)
    kapt(libs.hilt.compiler)

    // Jetpack Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.bundles.compose.standard)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.coil)
    implementation(libs.bundles.ktor)

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
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.bundles.compose.test)
    debugImplementation(libs.compose.test.manifest)
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> =
        listOf("-Aroom.schemaLocation=${schemaDir.path}")
}