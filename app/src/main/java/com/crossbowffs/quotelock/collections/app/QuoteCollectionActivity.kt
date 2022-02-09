package com.crossbowffs.quotelock.collections.app

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.imageLoader
import coil.request.ImageRequest
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.backup.LocalBackup
import com.crossbowffs.quotelock.backup.ProgressCallback
import com.crossbowffs.quotelock.backup.RemoteBackup
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionHelper
import com.crossbowffs.quotelock.utils.DpUtils

/**
 * @author Yubyf
 */
class QuoteCollectionActivity : AppCompatActivity() {

    private var mLoadingDialog: ProgressDialog? = null
    private var mMenu: Menu? = null
    private val mLocalBackupCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String) {
            Toast.makeText(
                applicationContext, "Local backup completed",
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
    private val mLocalRestoreCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String) {
            Toast.makeText(
                applicationContext, "Local restore completed", Toast.LENGTH_SHORT
            ).show()
            hideProgress()
            val fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            (fragment as? QuoteCollectionFragment)?.reloadData()
        }
    }
    private val mRemoteBackupCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String) {
            Toast.makeText(
                applicationContext, "Remote backup completed",
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
    private val mRemoteRestoreCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String) {
            Toast.makeText(
                applicationContext, "Remote restore completed",
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
            val fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            (fragment as? QuoteCollectionFragment)?.reloadData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, QuoteCollectionFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.collections_options, menu)
        mMenu = menu
        initMenu(mMenu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.account_action) {
            if (RemoteBackup.getInstance().isGoogleAccountSignedIn(this)) {
                RemoteBackup.getInstance().switchAccount(this, RemoteBackup.REQUEST_CODE_SIGN_IN,
                    object : ProgressCallback {
                        override fun inProcessing(message: String) {
                            showProgress(message)
                        }

                        override fun success(message: String) {
                            hideProgress()
                            initMenu(mMenu)
                            invalidateOptionsMenu()
                            if (!TextUtils.isEmpty(message)) {
                                SyncAccountManager.getInstance().removeAccount(message)
                            }
                        }

                        override fun failure(message: String) {
                            hideProgress()
                        }
                    })
            } else {
                RemoteBackup.getInstance().requestSignIn(this, RemoteBackup.REQUEST_CODE_SIGN_IN)
            }
        } else if (item.itemId == R.id.local_backup) {
            showProgress("Start local backup...")
            localBackup()
            return true
        } else if (item.itemId == R.id.local_restore) {
            showProgress("Start local restore...")
            localRestore()
            return true
        } else if (item.itemId == R.id.remote_backup) {
            showProgress("Start remote backup...")
            remoteBackup()
            return true
        } else if (item.itemId == R.id.remote_restore) {
            showProgress("Start remote restore...")
            remoteRestore()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocalBackup.REQUEST_CODE_PERMISSIONS_BACKUP) {
            LocalBackup.handleRequestPermissionsResult(
                grantResults,
                mLocalBackupCallback
            ) { localBackup() }
        } else if (requestCode == LocalBackup.REQUEST_CODE_PERMISSIONS_RESTORE) {
            LocalBackup.handleRequestPermissionsResult(
                grantResults,
                mLocalRestoreCallback
            ) { localRestore() }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(
                    this, data,
                    mRemoteBackupCallback
                ) {
                    Toast.makeText(
                        applicationContext,
                        "Successfully connected to Google account!", Toast.LENGTH_SHORT
                    ).show()
                    initMenu(mMenu)
                    invalidateOptionsMenu()
                    val accountName = RemoteBackup.getInstance().getSignedInGoogleAccountEmail(this)
                    if (!TextUtils.isEmpty(accountName)) {
                        SyncAccountManager.getInstance().addOrUpdateAccount(accountName)
                    }
                }
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_BACKUP) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(
                    this, data,
                    mRemoteBackupCallback
                ) { remoteBackup() }
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_RESTORE) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(
                    this, data,
                    mRemoteRestoreCallback
                ) { remoteRestore() }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        mLoadingDialog = null
        super.onDestroy()
    }

    private fun initMenu(menu: Menu?) {
        if (menu == null) {
            return
        }
        if (RemoteBackup.getInstance().isGoogleAccountSignedIn(this)) {
            menu.findItem(R.id.account).isVisible = true
            menu.findItem(R.id.account).title =
                RemoteBackup.getInstance().getSignedInGoogleAccountEmail(this)
            menu.findItem(R.id.account_action).setTitle(R.string.switch_account)
            menu.findItem(R.id.remote_backup).isEnabled = true
            menu.findItem(R.id.remote_restore).isEnabled = true
            val avatar = RemoteBackup.getInstance().getSignedInGoogleAccountPhoto(this)
            val iconSize = DpUtils.dp2px(this, 24f).toInt()
            applicationContext.imageLoader.enqueue(
                ImageRequest.Builder(applicationContext)
                    .data(avatar)
                    .size(iconSize)
                    .target(
                        onSuccess = { result ->
                            mMenu?.let {
                                it.findItem(R.id.account).icon = result
                                invalidateOptionsMenu()
                            }
                        }
                    ).build())
        } else {
            menu.findItem(R.id.account_action).setTitle(R.string.connect_account)
            menu.findItem(R.id.account).isVisible = false
            menu.findItem(R.id.remote_backup).isEnabled = false
            menu.findItem(R.id.remote_restore).isEnabled = false
        }
    }

    private fun localBackup() {
        LocalBackup.performBackup(
            this, QuoteCollectionHelper.DATABASE_NAME,
            mLocalBackupCallback
        )
    }

    private fun localRestore() {
        LocalBackup.performRestore(
            this, QuoteCollectionHelper.DATABASE_NAME,
            mLocalRestoreCallback
        )
    }

    private fun remoteBackup() {
        RemoteBackup.getInstance().performDriveBackupAsync(
            this,
            QuoteCollectionHelper.DATABASE_NAME, mRemoteBackupCallback
        )
    }

    private fun remoteRestore() {
        RemoteBackup.getInstance().performDriveRestoreAsync(
            this,
            QuoteCollectionHelper.DATABASE_NAME, mRemoteRestoreCallback
        )
    }

    private fun showProgress(message: String? = null) {
        val dialog = mLoadingDialog ?: (ProgressDialog(this).also { mLoadingDialog = it })
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(message)
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideProgress() {
        mLoadingDialog?.let { if (!it.isShowing) null else it }?.dismiss()
    }

    private abstract inner class AbstractBackupCallback : ProgressCallback {
        override fun inProcessing(message: String) {
            showProgress(message)
        }

        override fun failure(message: String) {
            Toast.makeText(
                applicationContext, message, Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
}