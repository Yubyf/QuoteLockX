package com.crossbowffs.quotelock.modules.brainyquote.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.modules.brainyquote.brainyDataStore
import com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys
import kotlinx.coroutines.runBlocking

class BrainyQuoteConfigActivity : Activity(), RadioGroup.OnCheckedChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.radio_brainy_quote)
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