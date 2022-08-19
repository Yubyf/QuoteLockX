package com.crossbowffs.quotelock.app.history

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.ui.navigation.QuoteNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                QuoteNavHost {
                    finish()
                }
            }
        }
    }
}