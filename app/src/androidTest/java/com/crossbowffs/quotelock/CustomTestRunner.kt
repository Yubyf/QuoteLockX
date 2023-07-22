package com.crossbowffs.quotelock

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.crossbowffs.quotelock.app.TestApp

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestApp::class.java.name, context)
    }
}