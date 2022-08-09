package com.crossbowffs.quotelock.app.configs.fortune

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.di.FortuneDataStore
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.datastore.DataStoreDelegate
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class FortuneConfigActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    @Inject
    @FortuneDataStore
    lateinit var fortuneDataStore: DataStoreDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.radio_fortune)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { onBackPressed() }

        val radioGroup = findViewById<RadioGroup>(R.id.module_fortune_activity_radiogroup)
        val queryValueIndex = runBlocking {
            fortuneDataStore.getIntSuspend(FortunePrefKeys.PREF_FORTUNE_CATEGORY_INT, 0)
        }
        resources.getStringArray(R.array.fortune_categories).forEach {
            val radioButton =
                layoutInflater.inflate(R.layout.radio_item, radioGroup, false) as RadioButton
            radioGroup.addView(radioButton.apply {
                text = it.replaceFirstChar { it.titlecase() }
                id = View.generateViewId()
            }, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val button = radioGroup.getChildAt(queryValueIndex) as RadioButton
        button.isChecked = true
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val queryValueArray = resources.getStringArray(R.array.fortune_categories)
        val queryValueIndex = group.indexOfChild(findViewById(checkedId))
        val queryValue = queryValueArray[queryValueIndex]
        fortuneDataStore.bulkPut(
            mapOf(FortunePrefKeys.PREF_FORTUNE_CATEGORY_INT to queryValueIndex,
                FortunePrefKeys.PREF_FORTUNE_CATEGORY_STRING to queryValue)
        )
    }
}