package com.crossbowffs.quotelock.history.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.history.app.QuoteHistoryFragment.Companion.BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE
import com.crossbowffs.quotelock.history.app.QuoteHistoryFragment.Companion.REQUEST_KEY_HISTORY_LIST_PAGE
import com.crossbowffs.quotelock.history.database.quoteHistoryDatabase
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class QuoteHistoryActivity : AppCompatActivity() {
    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        supportFragmentManager.apply {
            beginTransaction()
                .add(R.id.content_frame, QuoteHistoryFragment())
                .commit()
            // Hide menu items while details are shown
            setFragmentResultListener(REQUEST_KEY_HISTORY_LIST_PAGE,
                this@QuoteHistoryActivity) { _, bundle ->
                val result = bundle.getBoolean(BUNDLE_KEY_HISTORY_SHOW_DETAIL_PAGE, false)
                mMenu?.findItem(R.id.clear)?.isVisible = !result
            }
        }

        ioScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                quoteHistoryDatabase.dao().count().collect {
                    withContext(Dispatchers.Main) {
                        mMenu?.findItem(R.id.clear)?.isVisible = it != 0
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.histories_options, menu)
        mMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear) {
            clearQuoteHistories()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearQuoteHistories() {
        mMenu?.findItem(R.id.clear)?.isEnabled = false
        ioScope.launch {
            quoteHistoryDatabase.dao().deleteAll()
            withContext(Dispatchers.Main) {
                mMenu?.findItem(R.id.clear)?.isEnabled = true
                Toast.makeText(this@QuoteHistoryActivity,
                    R.string.quote_histories_cleared_quote,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}