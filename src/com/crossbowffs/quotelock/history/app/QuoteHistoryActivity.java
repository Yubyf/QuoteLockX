package com.crossbowffs.quotelock.history.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.crossbowffs.quotelock.R;

/**
 * @author Yubyf
 */
public class QuoteHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new QuoteHistoryFragment())
                .commit();
    }
}
