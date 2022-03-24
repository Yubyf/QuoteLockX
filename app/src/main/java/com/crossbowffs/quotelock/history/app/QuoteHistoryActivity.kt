package com.crossbowffs.quotelock.history.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.history.provider.QuoteHistoryContract
import com.crossbowffs.quotelock.utils.ioScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Yubyf
 */
class QuoteHistoryActivity : AppCompatActivity() {
    private lateinit var mMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, QuoteHistoryFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.histories_options, menu)
        mMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        clearQuoteHistories()
        return super.onOptionsItemSelected(item)
    }

    private fun clearQuoteHistories() {
        mMenu.findItem(R.id.clear)?.isEnabled = false
        ioScope.launch {
            contentResolver.delete(QuoteHistoryContract.Histories.CONTENT_URI, null, null)
            withContext(Dispatchers.Main) {
                mMenu.findItem(R.id.clear)?.isEnabled = true
                Toast.makeText(this@QuoteHistoryActivity,
                    R.string.quote_histories_cleared_quote,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}