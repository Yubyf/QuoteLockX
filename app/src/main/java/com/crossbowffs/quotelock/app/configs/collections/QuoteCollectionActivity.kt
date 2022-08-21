package com.crossbowffs.quotelock.app.configs.collections

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.ui.navigation.QuoteCollectionNavHost
import com.crossbowffs.quotelock.ui.theme.QuoteLockTheme
import dagger.hilt.android.AndroidEntryPoint


/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteCollectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuoteLockTheme {
                QuoteCollectionNavHost {
                    finish()
                }
            }
        }
    }
}