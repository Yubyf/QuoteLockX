package com.crossbowffs.quotelock.account.google

import android.content.Context
import android.content.Intent
import com.crossbowffs.quotelock.data.api.GoogleAccount
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * @author Yubyf
 */
class GoogleAccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun checkGooglePlayService(): Boolean {
        return GoogleAccountHelper.checkGooglePlayService(context)
    }

    fun getGoogleAccount(): GoogleSignInAccount = GoogleAccountHelper.getGoogleAccount(context)

    fun isGoogleAccountSignedIn(): Boolean = GoogleAccountHelper.isGoogleAccountSignedIn(context)

    fun getSignedInGoogleAccount(): GoogleAccount? =
        GoogleAccountHelper.getSignedInGoogleAccount(context)

    fun getSignInIntent(): Intent = GoogleAccountHelper.getSignInIntent(context)

    suspend fun signOutAccount() = GoogleAccountHelper.signOutAccount(context)

    suspend fun handleSignInResult(result: Intent?): GoogleAccount? =
        GoogleAccountHelper.handleSignInResult(result)
}