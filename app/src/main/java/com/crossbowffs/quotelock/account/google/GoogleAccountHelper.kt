package com.crossbowffs.quotelock.account.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.crossbowffs.quotelock.utils.Xlog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

/**
 * @author Yubyf
 */
object GoogleAccountHelper {
    private const val TAG = "GoogleAccountHelper"

    private val GDRIVE_SCOPE: Scope by lazy { Scope(DriveScopes.DRIVE_FILE) }

    fun checkGooglePlayService(context: Context): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun getGoogleAccount(context: Context): GoogleSignInAccount =
        GoogleSignIn.getAccountForScopes(context, GDRIVE_SCOPE)

    fun isGoogleAccountSignedIn(context: Context): Boolean {
        return checkGooglePlayService(context)
                && GoogleSignIn.hasPermissions(getGoogleAccount(context), GDRIVE_SCOPE)
    }

    fun getSignedInGoogleAccountEmail(context: Context): String? {
        return getGoogleAccount(context).email
    }

    fun getSignedInGoogleAccountPhoto(context: Context): Uri? {
        return getGoogleAccount(context).photoUrl
    }

    /**
     * Starts a sign-in activity using [.REQUEST_CODE_SIGN_IN],
     * [.REQUEST_CODE_SIGN_IN_BACKUP] or [.REQUEST_CODE_SIGN_IN_RESTORE].
     */
    fun requestSignIn(activity: Activity, resultLauncher: ActivityResultLauncher<Intent>) {
        Xlog.d(TAG, "Requesting sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .requestScopes(GDRIVE_SCOPE)
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        // The result of the sign-in Intent is handled in ActivityResultCallback.
        resultLauncher.launch(client.signInIntent)
    }

    fun signOutAccount(
        activity: Activity,
        resultAction: (success: Boolean, message: String) -> Unit,
    ) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        val signedEmail = getSignedInGoogleAccountEmail(activity)
        client.signOut().addOnCompleteListener {
            resultAction.invoke(true, signedEmail ?: "")
        }.addOnFailureListener {
            resultAction.invoke(false, it.message ?: "")
        }
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    fun handleSignInResult(
        result: Intent?,
        resultAction: (success: Boolean) -> Unit,
    ) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener {
                Xlog.d(TAG, "Signed in as " + it.email)
                resultAction.invoke(true)
            }
            .addOnFailureListener {
                Xlog.e(TAG, "Unable to sign in.", it)
                resultAction.invoke(false)
            }
    }
}