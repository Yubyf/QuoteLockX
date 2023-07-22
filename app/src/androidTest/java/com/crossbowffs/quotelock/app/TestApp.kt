package com.crossbowffs.quotelock.app

import android.app.Application
import com.crossbowffs.quotelock.di.DataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApp)
            modules(DataModule().module)
        }
    }
}