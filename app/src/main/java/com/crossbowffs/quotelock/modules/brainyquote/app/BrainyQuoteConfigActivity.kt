package com.crossbowffs.quotelock.modules.brainyquote.app

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys

class BrainyQuoteConfigActivity : Activity(), RadioGroup.OnCheckedChangeListener {

    private var mBrainyquotePreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBrainyquotePreferences =
            getSharedPreferences(BrainyQuotePrefKeys.PREF_BRAINY, MODE_PRIVATE)
        setContentView(R.layout.radio_brainy_quote)
        val radioGroup = findViewById<View>(R.id.module_brainy_activity_radiogroup) as RadioGroup
        mBrainyquotePreferences?.run {
            val queryValueIndex = getInt(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT, 0)
            val button = radioGroup.getChildAt(queryValueIndex) as RadioButton
            button.isChecked = true
        }
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val queryValueArray = resources.getStringArray(R.array.brainy_quote_type_values)
        val queryValueIndex = group.indexOfChild(findViewById(checkedId))
        val queryValue = queryValueArray[queryValueIndex]
        mBrainyquotePreferences?.run {
            edit()
                .putInt(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT, queryValueIndex)
                .putString(BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING, queryValue)
                .apply()
        }
    }
}