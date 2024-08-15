import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `kotlin-dsl-base`
}

group = "com.crossbowffs.quotelockx.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    compileOnly(libs.compose.compiler.plugin)
    // Reference libs.version.toml in the build logic module
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "quotelockx.android.application"
            implementationClass = "AndroidApplicationPlugin"
        }

        register("androidApplicationCompose") {
            id = "quotelockx.android.application.compose"
            implementationClass = "AndroidApplicationComposePlugin"
        }

        register("androidApplicationJacoco") {
            id = "quotelockx.android.application.jacoco"
            implementationClass = "AndroidApplicationJacocoPlugin"
        }

        register("androidLibrary") {
            id = "quotelockx.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }

        register("androidLibraryCompose") {
            id = "quotelockx.android.library.compose"
            implementationClass = "AndroidLibraryComposePlugin"
        }

        register("androidLibraryJacoco") {
            id = "quotelockx.android.library.jacoco"
            implementationClass = "AndroidLibraryJacocoPlugin"
        }

        register("androidHilt") {
            id = "quotelockx.android.hilt"
            implementationClass = "AndroidHiltPlugin"
        }

        register("androidFeature") {
            id = "quotelockx.android.feature"
            implementationClass = "AndroidFeaturePlugin"
        }

        register("androidRoom") {
            id = "quotelockx.android.room"
            implementationClass = "AndroidRoomPlugin"
        }
    }
}