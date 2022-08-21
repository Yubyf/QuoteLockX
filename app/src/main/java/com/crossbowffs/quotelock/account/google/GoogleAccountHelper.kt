package com.crossbowffs.quotelock.account.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.crossbowffs.quotelock.utils.Xlog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    fun getSignedInGoogleAccount(context: Context): GoogleAccount? =
        if (!isGoogleAccountSignedIn(context)) null
        else getGoogleAccount(context).let {
            it.email?.let { email ->
                GoogleAccount(email, it.photoUrl)
            }
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
    fun getSignInIntent(context: Context): Intent {
        Xlog.d(TAG, "Requesting Google account sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .requestScopes(GDRIVE_SCOPE)
            .build()
        return GoogleSignIn.getClient(context, signInOptions).signInIntent
    }

    suspend fun signOutAccount(context: Context): Pair<Boolean, String> =
        suspendCoroutine { continuation ->
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(context, signInOptions)
            val signedEmail = getSignedInGoogleAccountEmail(context)
            client.signOut().addOnCompleteListener {
                continuation.resume(Pair(true, signedEmail ?: ""))
            }.addOnFailureListener {
                continuation.resume(Pair(false, it.message ?: ""))
            }
        }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [getSignInIntent].
     */
    suspend fun handleSignInResult(result: Intent?): GoogleAccount? =
        suspendCoroutine { continuation ->
            GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { googleSignInAccount ->
                    Xlog.d(TAG, "Signed in as " + googleSignInAccount.email)
                    continuation.resume(googleSignInAccount.email?.let { it ->
                        GoogleAccount(it, googleSignInAccount.photoUrl)
                    })
                }
                .addOnFailureListener {
                    Xlog.e(TAG, "Unable to sign in.", it)
                    continuation.resume(null)
                }
        }
}