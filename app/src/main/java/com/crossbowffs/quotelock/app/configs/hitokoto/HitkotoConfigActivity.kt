package com.crossbowffs.quotelock.app.configs.hitokoto

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.di.HitokotoDataStore
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.datastore.DataStoreDelegate
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class HitkotoConfigActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    @Inject
    @HitokotoDataStore
    lateinit var hitokotoDataStore: DataStoreDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.radio_hitokoto_quote)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { onBackPressed() }

        val radioGroup = findViewById<View>(R.id.module_hitokoto_activity_radiogroup) as RadioGroup
        val queryValueIndex = runBlocking {
            hitokotoDataStore.getIntSuspend(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_INT,
                0)
        }
        val button = radioGroup.getChildAt(queryValueIndex) as RadioButton
        button.isChecked = true
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val queryValueArray = resources.getStringArray(R.array.hitokoto_type_values)
        val queryValueIndex = group.indexOfChild(findViewById(checkedId))
        val queryValue = queryValueArray[queryValueIndex]
        hitokotoDataStore.run {
            put(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_INT, queryValueIndex)
            put(HitokotoPrefKeys.PREF_HITOKOTO_TYPE_STRING, queryValue)
        }
    }
}