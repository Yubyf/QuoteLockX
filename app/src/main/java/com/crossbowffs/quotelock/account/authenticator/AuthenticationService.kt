package com.crossbowffs.quotelock.account.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.crossbowffs.quotelock.utils.Xlog
import com.crossbowffs.quotelock.utils.className

/**
 * @author Yubyf
 */
class AuthenticationService : Service() {

    private lateinit var mAuthenticator: Authenticator

    override fun onCreate() {
        Xlog.v(TAG, "SampleSyncAdapter Authentication Service started.")
        mAuthenticator = Authenticator(this)
    }

    override fun onDestroy() {
        Xlog.v(TAG, "SampleSyncAdapter Authentication Service stopped.")
    }

    override fun onBind(intent: Intent): IBinder? {
        Xlog.v(TAG, "getBinder()...  returning the AccountAuthenticator binder for intent "
                + intent)
        return mAuthenticator.iBinder
    }

    companion object {
        private val TAG = className<AuthenticationService>()
    }
}