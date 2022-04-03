package com.crossbowffs.quotelock.history.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.history.app.QuoteHistoryFragment.Companion.BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE
import com.crossbowffs.quotelock.history.app.QuoteHistoryFragment.Companion.REQUEST_KEY_HISTORY_LIST_PAGE
import com.crossbowffs.quotelock.history.database.quoteHistoryDatabase
import com.crossbowffs.quotelock.utils.ioScope
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class QuoteHistoryActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_histories_activity_label)
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24dp)
            inflateMenu(R.menu.histories_options)
            setNavigationOnClickListener { onBackPressed() }
            setOnMenuItemClickListener {
                if (it.itemId == R.id.clear) {
                    clearQuoteHistories()
                }
                true
            }
        }

        supportFragmentManager.apply {
            beginTransaction()
                .add(R.id.content_frame, QuoteHistoryFragment())
                .commit()
            // Hide menu items while details are shown
            setFragmentResultListener(REQUEST_KEY_HISTORY_LIST_PAGE,
                this@QuoteHistoryActivity) { _, bundle ->
                val result = bundle.getBoolean(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE, false)
                toolbar.menu?.findItem(R.id.clear)?.isVisible = !result
            }
        }

        ioScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                quoteHistoryDatabase.dao().count().collect {
                    withContext(Dispatchers.Main) {
                        toolbar.menu?.findItem(R.id.clear)?.isVisible = it != 0
                    }
                }
            }
        }
    }

    private fun clearQuoteHistories() {
        toolbar.menu?.findItem(R.id.clear)?.isEnabled = false
        ioScope.launch {
            quoteHistoryDatabase.dao().deleteAll()
            withContext(Dispatchers.Main) {
                toolbar.menu?.findItem(R.id.clear)?.isEnabled = true
                Toast.makeText(this@QuoteHistoryActivity,
                    R.string.quote_histories_cleared_quote,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}