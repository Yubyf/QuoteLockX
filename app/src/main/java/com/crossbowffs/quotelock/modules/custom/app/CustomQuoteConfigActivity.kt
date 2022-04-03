package com.crossbowffs.quotelock.modules.custom.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R

class CustomQuoteConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.module_custom_activity_label)
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24dp)
            inflateMenu(R.menu.custom_quote_options)
            setNavigationOnClickListener { onBackPressed() }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, CustomQuoteConfigFragment())
            .commit()
    }
}