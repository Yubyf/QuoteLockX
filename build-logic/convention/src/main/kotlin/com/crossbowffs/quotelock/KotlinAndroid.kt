package com.crossbowffs.quotelock

import com.android.build.api.dsl.CommonExtension

internal fun CommonExtension<*, *, *, *, *, *>.configureKotlinAndroid() {
    compileSdk = Configs.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = Configs.MIN_SDK_VERSION
    }

    compileOptions {
        sourceCompatibility = Configs.javaVersion
        targetCompatibility = Configs.javaVersion
    }
}