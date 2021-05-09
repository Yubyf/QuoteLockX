package com.crossbowffs.quotelock.collections.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.crossbowffs.quotelock.R;

/**
 * @author Yubyf
 */
public class QuoteCollectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new QuoteCollectionFragment())
                .commit();
    }
}
