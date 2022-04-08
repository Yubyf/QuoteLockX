plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.yubyf.quotelockx"
        versionCode = 18
        versionName = "2.0.3"
        minSdk = 21

        targetSdk = 29

        buildConfigField("int", "MODULE_VERSION", "3")
        buildConfigField("int", "CUSTOM_QUOTES_DB_VERSION", "4")
        buildConfigField("int", "QUOTE_COLLECTIONS_DB_VERSION", "4")
        buildConfigField("int", "QUOTE_HISTORIES_DB_VERSION", "4")
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
            // Enables code shrinking.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            buildConfigField("int", "LOG_LEVEL", "4")
            buildConfigField("boolean", "LOG_TO_XPOSED", "true")
        }
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
}

dependencies {
    implementation("androidx.core:core:1.7.0")
    implementation("com.crossbowffs.remotepreferences:remotepreferences:0.9")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("io.github.yubyf.datastorepreferences:datastorepreferences:1.2.1")
    implementation("com.google.android.material:material:1.5.0")
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("de.robv.android.xposed:api:82:sources")

    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    // Fix the conflict between concurrent-futures and guava libs.
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation("io.coil-kt:coil:1.4.0")

    implementation("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.api-client:google-api-client-android:1.33.2")
    implementation("com.google.apis:google-api-services-drive:v3-rev20220214-1.32.1")

    // Room
    val roomVersion = "2.4.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("com.opencsv:opencsv:5.6")
}