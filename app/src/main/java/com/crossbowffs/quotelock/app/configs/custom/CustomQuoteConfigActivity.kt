package com.crossbowffs.quotelock.app.configs.custom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomQuoteConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.module_custom_activity_label)
            setNavigationIcon(R.drawable.ic_round_arrow_back_24dp)
        }
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, CustomQuoteConfigFragment())
            .commit()
    }
}