package com.crossbowffs.quotelock.app;

import android.app.Activity;
import android.os.Bundle;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.preferences.PrefKeys;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, new SettingsFragment())
            .commit();
    }
}
