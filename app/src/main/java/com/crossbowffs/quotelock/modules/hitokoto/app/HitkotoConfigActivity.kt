package com.crossbowffs.quotelock.modules.hitokoto.app

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys

class HitkotoConfigActivity : Activity(), RadioGroup.OnCheckedChangeListener {

    private var mHitokotoPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHitokotoPreferences = getSharedPreferences(HitokotoPrefKeys.PREF_HITOKOTO, MODE_PRIVATE)
        setContentView(R.layout.radio_hitokoto_quote)
        val radioGroup = findViewById<View>(R.id.module_hitokoto_activity_radiogroup) as RadioGroup
        mHitokotoPreferences?.run {
            val queryValueIndex = getInt(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_INT, 0)
            val button = radioGroup.getChildAt(queryValueIndex) as RadioButton
            button.isChecked = true
        }
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val queryValueArray = resources.getStringArray(R.array.hitokoto_type_values)
        val queryValueIndex = group.indexOfChild(findViewById(checkedId))
        val queryValue = queryValueArray[queryValueIndex]
        mHitokotoPreferences?.run {
            edit()
                .putInt(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_INT, queryValueIndex)
                .putString(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_STRING, queryValue)
                .apply()
        }
    }
}