package com.crossbowffs.quotelock.app.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.ui.navigation.MainNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                MainNavHost()
            }
        }
    }
}