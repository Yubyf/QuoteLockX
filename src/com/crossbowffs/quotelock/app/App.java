package com.crossbowffs.quotelock.app;

import android.app.Application;

import com.jinrishici.sdk.android.factory.JinrishiciFactory;

/**
 * @author Yubyf
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JinrishiciFactory.init(this);
    }
}
