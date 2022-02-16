package com.crossbowffs.quotelock.account.authenticator

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity
import com.crossbowffs.quotelock.utils.className

internal class Authenticator(
    /** Authentication Service context  */
    private val mContext: Context,
) : AbstractAccountAuthenticator(mContext) {

    override fun addAccount(
        response: AccountAuthenticatorResponse, accountType: String,
        authTokenType: String, requiredFeatures: Array<String>, options: Bundle,
    ): Bundle {
        val intent = Intent(mContext, QuoteCollectionActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse, account: Account, options: Bundle,
    ): Bundle? {
        return null
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse,
        accountType: String,
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String, loginOptions: Bundle,
    ): Bundle? {
        return null
    }

    override fun getAuthTokenLabel(authTokenType: String): String? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse, account: Account, features: Array<String>,
    ): Bundle {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return result
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String, loginOptions: Bundle,
    ): Bundle? {
        return null
    }

    companion object {
        private val TAG = className<Authenticator>()
    }
}