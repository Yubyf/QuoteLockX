package com.crossbowffs.quotelock.data

import android.content.Context
import com.crossbowffs.quotelock.di.DataModule
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class CheckModulesTest : KoinTest {

    @Test
    fun checkAllModules() {
        DataModule().module.verify(
            extraTypes = listOf(
                HttpClientEngine::class,
                HttpClientConfig::class,
                Context::class
            )
        )
    }
}