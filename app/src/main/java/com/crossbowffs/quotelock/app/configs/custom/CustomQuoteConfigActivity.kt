package com.crossbowffs.quotelock.app.configs.custom

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.ui.navigation.CustomQuoteNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomQuoteConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                CustomQuoteNavHost {
                    finish()
                }
            }
        }
    }
}