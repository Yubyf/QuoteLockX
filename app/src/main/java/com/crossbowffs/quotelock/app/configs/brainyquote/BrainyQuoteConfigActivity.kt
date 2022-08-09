package com.crossbowffs.quotelock.app.configs.brainyquote

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.di.BrainyDataStore
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.datastore.DataStoreDelegate
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BrainyQuoteConfigActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    @Inject
    @BrainyDataStore
    lateinit var brainyDataStore: DataStoreDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.radio_brainy_quote)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { onBackPressed() }

        val radioGroup = findViewById<View>(R.id.module_brainy_activity_radiogroup) as RadioGroup
        val queryValueIndex = runBlocking {
            brainyDataStore.getIntSuspend(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT,
                0)
        }
        val button = radioGroup.getChildAt(queryValueIndex) as RadioButton
        button.isChecked = true
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val queryValueArray = resources.getStringArray(R.array.brainy_quote_type_values)
        val queryValueIndex = group.indexOfChild(findViewById(checkedId))
        val queryValue = queryValueArray[queryValueIndex]
        brainyDataStore.run {
            put(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT, queryValueIndex)
            put(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING, queryValue)
        }
    }
}