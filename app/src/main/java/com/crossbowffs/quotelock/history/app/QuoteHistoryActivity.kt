package com.crossbowffs.quotelock.history.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.R

/**
 * @author Yubyf
 */
class QuoteHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, QuoteHistoryFragment())
            .commit()
    }
}