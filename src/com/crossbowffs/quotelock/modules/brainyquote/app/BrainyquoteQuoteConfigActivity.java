package com.crossbowffs.quotelock.modules.brainyquote.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.crossbowffs.quotelock.R;

import static com.crossbowffs.quotelock.consts.PrefKeys.PREF_QUOTES;
import static com.crossbowffs.quotelock.consts.PrefKeys.PREF_QUOTES_BRAINY_TYPE_STRING;
import static com.crossbowffs.quotelock.consts.PrefKeys.PREF_QUOTES_BRAINY_TYPE_INT;

public class BrainyquoteQuoteConfigActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private SharedPreferences brainyquotePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        brainyquotePreferences = getSharedPreferences(PREF_QUOTES, Context.MODE_PRIVATE);
        setContentView(R.layout.radio_brainy_quote);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.module_brainy_activity_radiogroup);

        RadioButton button = (RadioButton)radioGroup
            .getChildAt(brainyquotePreferences.getInt(PREF_QUOTES_BRAINY_TYPE_INT, 0));

        button.setChecked(true);

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String[] queryValueArray = getResources().getStringArray(R.array.brainy_quote_type_values);
        int queryValueIndex = group.indexOfChild(findViewById(checkedId));

        String queryValue = queryValueArray[queryValueIndex];

        brainyquotePreferences.edit().
                putInt(PREF_QUOTES_BRAINY_TYPE_INT, queryValueIndex).
                putString(PREF_QUOTES_BRAINY_TYPE_STRING, queryValue).commit();
    }
}
