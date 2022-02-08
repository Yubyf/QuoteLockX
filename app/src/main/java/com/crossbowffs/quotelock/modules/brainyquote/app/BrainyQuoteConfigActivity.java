package com.crossbowffs.quotelock.modules.brainyquote.app;

import static com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY;
import static com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_INT;
import static com.crossbowffs.quotelock.modules.brainyquote.consts.BrainyQuotePrefKeys.PREF_BRAINY_TYPE_STRING;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.crossbowffs.quotelock.R;

public class BrainyQuoteConfigActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private SharedPreferences mBrainyquotePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBrainyquotePreferences = getSharedPreferences(PREF_BRAINY, Context.MODE_PRIVATE);
        setContentView(R.layout.radio_brainy_quote);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.module_brainy_activity_radiogroup);
        int queryValueIndex = mBrainyquotePreferences.getInt(PREF_BRAINY_TYPE_INT, 0);
        RadioButton button = (RadioButton)radioGroup.getChildAt(queryValueIndex);
        button.setChecked(true);

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String[] queryValueArray = getResources().getStringArray(R.array.brainy_quote_type_values);
        int queryValueIndex = group.indexOfChild(findViewById(checkedId));
        String queryValue = queryValueArray[queryValueIndex];

        mBrainyquotePreferences.edit()
            .putInt(PREF_BRAINY_TYPE_INT, queryValueIndex)
            .putString(PREF_BRAINY_TYPE_STRING, queryValue)
            .apply();
    }
}
