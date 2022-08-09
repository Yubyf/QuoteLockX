package com.crossbowffs.quotelock.app.configs.collections

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint


/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteCollectionActivity : AppCompatActivity() {

    private lateinit var container: View
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        container = findViewById(R.id.content_frame)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_collections_activity_label)
            setNavigationIcon(R.drawable.ic_round_arrow_back_24dp)
        }
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        supportFragmentManager.apply {
            beginTransaction()
                .add(R.id.content_frame, QuoteCollectionFragment())
                .commit()
            setFragmentResultListener(QuoteCollectionFragment.REQUEST_KEY_COLLECTION_LIST_PAGE,
                this@QuoteCollectionActivity) { _, bundle ->
                // Hide menu items while details are shown
                val result =
                    bundle.getBoolean(QuoteCollectionFragment.BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE,
                        false)
                toolbar.setTitle(if (result) R.string.pref_detail_title else R.string.quote_collections_activity_label)
            }
        }
    }
}