package com.crossbowffs.quotelock.app.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.app.history.QuoteHistoryFragment.Companion.BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE
import com.crossbowffs.quotelock.app.history.QuoteHistoryFragment.Companion.REQUEST_KEY_HISTORY_LIST_PAGE
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteHistoryActivity : AppCompatActivity() {
    private lateinit var container: View
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        container = findViewById(R.id.content_frame)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_histories_activity_label)
            setNavigationIcon(R.drawable.ic_round_arrow_back_24dp)
        }
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        supportFragmentManager.apply {
            beginTransaction()
                .add(R.id.content_frame, QuoteHistoryFragment())
                .commit()
            // Change title when fragment is changed
            setFragmentResultListener(REQUEST_KEY_HISTORY_LIST_PAGE,
                this@QuoteHistoryActivity) { _, bundle ->
                val result = bundle.getBoolean(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE, false)
                toolbar.setTitle(if (result) R.string.pref_detail_title else R.string.quote_histories_activity_label)
            }
        }
    }
}