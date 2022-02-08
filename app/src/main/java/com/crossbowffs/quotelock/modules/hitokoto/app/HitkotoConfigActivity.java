package com.crossbowffs.quotelock.modules.hitokoto.app;

import static com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO;
import static com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO_TYPE_INT;
import static com.crossbowffs.quotelock.modules.hitokoto.consts.HitokotoPrefKeys.PREF_HITOKOTO_TYPE_STRING;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.crossbowffs.quotelock.R;

public class HitkotoConfigActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private SharedPreferences mHitokotoPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHitokotoPreferences = getSharedPreferences(PREF_HITOKOTO, Context.MODE_PRIVATE);
        setContentView(R.layout.radio_hitokoto_quote);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.module_hitokoto_activity_radiogroup);
        int queryValueIndex = mHitokotoPreferences.getInt(PREF_HITOKOTO_TYPE_INT, 0);
        RadioButton button = (RadioButton)radioGroup.getChildAt(queryValueIndex);
        button.setChecked(true);

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String[] queryValueArray = getResources().getStringArray(R.array.hitokoto_type_values);
        int queryValueIndex = group.indexOfChild(findViewById(checkedId));
        String queryValue = queryValueArray[queryValueIndex];

        mHitokotoPreferences.edit()
            .putInt(PREF_HITOKOTO_TYPE_INT, queryValueIndex)
            .putString(PREF_HITOKOTO_TYPE_STRING, queryValue)
            .apply();
    }
}
