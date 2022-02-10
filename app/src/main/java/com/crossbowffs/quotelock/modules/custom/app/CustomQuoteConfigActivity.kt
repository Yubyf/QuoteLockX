package com.crossbowffs.quotelock.modules.custom.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.R

class CustomQuoteConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, CustomQuoteConfigFragment())
            .commit()
    }
}