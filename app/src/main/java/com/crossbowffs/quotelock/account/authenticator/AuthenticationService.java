package com.crossbowffs.quotelock.account.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.crossbowffs.quotelock.utils.Xlog;

/**
 * @author liupengyu
 */
public class AuthenticationService extends Service {

    private static final String TAG = "AuthenticationService";

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        Xlog.v(TAG, "SampleSyncAdapter Authentication Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        Xlog.v(TAG, "SampleSyncAdapter Authentication Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Xlog.v(TAG, "getBinder()...  returning the AccountAuthenticator binder for intent "
                + intent);
        return mAuthenticator.getIBinder();
    }
}
