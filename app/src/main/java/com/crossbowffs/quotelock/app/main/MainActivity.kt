package com.crossbowffs.quotelock.app.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.crossbowffs.quotelock.ui.navigation.MainNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                MainNavHost()
            }
        }
    }
}