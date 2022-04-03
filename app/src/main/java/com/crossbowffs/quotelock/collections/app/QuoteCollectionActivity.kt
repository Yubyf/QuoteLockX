package com.crossbowffs.quotelock.collections.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import coil.imageLoader
import coil.request.ImageRequest
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.backup.LocalBackup
import com.crossbowffs.quotelock.backup.ProgressCallback
import com.crossbowffs.quotelock.backup.RemoteBackup
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.components.ProgressAlertDialog
import com.crossbowffs.quotelock.utils.dp2px
import com.google.android.material.appbar.MaterialToolbar
import com.yubyf.quotelockx.R
import java.io.File

/**
 * @author Yubyf
 */
class QuoteCollectionActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private var mLoadingDialog: ProgressAlertDialog? = null
    private val mLocalBackupCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String?) {
            Toast.makeText(applicationContext,
                getString(R.string.backup_local_backup_completed) + "\nFile saved to " + message,
                Toast.LENGTH_LONG
            ).show()
            hideProgress()
        }
    }
    private val mLocalRestoreCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String?) {
            Toast.makeText(applicationContext,
                R.string.backup_local_restore_completed,
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
    private val mRemoteBackupCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String?) {
            Toast.makeText(
                applicationContext, R.string.backup_remote_backup_completed,
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
    private val mRemoteRestoreCallback: ProgressCallback = object : AbstractBackupCallback() {
        override fun success(message: String?) {
            Toast.makeText(
                applicationContext, R.string.backup_remote_restore_completed,
                Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }

    private val menuItemClickListener: OnMenuItemClickListener = object : OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if (item == null) {
                return false
            }
            when (item.itemId) {
                R.id.account_action -> {
                    if (RemoteBackup.INSTANCE.isGoogleAccountSignedIn(this@QuoteCollectionActivity)) {
                        RemoteBackup.INSTANCE.switchAccount(this@QuoteCollectionActivity,
                            RemoteBackup.REQUEST_CODE_SIGN_IN,
                            object : ProgressCallback {
                                override fun inProcessing(message: String?) {
                                    showProgress(message)
                                }

                                override fun success(message: String?) {
                                    hideProgress()
                                    initMenu(toolbar.menu)
                                    invalidateOptionsMenu()
                                    if (!message.isNullOrEmpty()) {
                                        SyncAccountManager.instance.removeAccount(message)
                                    }
                                }

                                override fun failure(message: String?) {
                                    hideProgress()
                                }
                            })
                    } else {
                        RemoteBackup.INSTANCE.requestSignIn(this@QuoteCollectionActivity,
                            RemoteBackup.REQUEST_CODE_SIGN_IN)
                    }
                }
                R.id.local_backup -> {
                    showProgress("Start local backup...")
                    localBackup()
                    return true
                }
                R.id.local_restore -> {
                    pickFile()
                    return true
                }
                R.id.remote_backup -> {
                    showProgress("Start remote backup...")
                    remoteBackup()
                    return true
                }
                R.id.remote_restore -> {
                    showProgress("Start remote restore...")
                    remoteRestore()
                    return true
                }
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_collections_activity_label)
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24dp)
            inflateMenu(R.menu.collections_options)
            initMenu(menu)
            setNavigationOnClickListener { onBackPressed() }
            setOnMenuItemClickListener(menuItemClickListener)
        }

        supportFragmentManager.apply {
            beginTransaction()
                .add(R.id.content_frame, QuoteCollectionFragment())
                .commit()
            setFragmentResultListener(QuoteCollectionFragment.REQUEST_KEY_COLLECTION_LIST_PAGE,
                this@QuoteCollectionActivity) { _, bundle ->
                // Hide menu items while details are shown
                val result =
                    bundle.getBoolean(QuoteCollectionFragment.BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE,
                        false)
                toolbar.menu?.setGroupVisible(R.id.backup_group, !result)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocalBackup.REQUEST_CODE_PERMISSIONS_BACKUP) {
            LocalBackup.handleRequestPermissionsResult(
                grantResults,
                mLocalBackupCallback
            ) { localBackup() }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.INSTANCE.handleSignInResult(
                    this, data,
                    mRemoteBackupCallback
                ) {
                    Toast.makeText(
                        applicationContext,
                        R.string.backup_google_account_connected, Toast.LENGTH_SHORT
                    ).show()
                    initMenu(toolbar.menu)
                    invalidateOptionsMenu()
                    val accountName = RemoteBackup.INSTANCE.getSignedInGoogleAccountEmail(this)
                    if (!accountName.isNullOrEmpty()) {
                        SyncAccountManager.instance.addOrUpdateAccount(accountName)
                    }
                }
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_BACKUP) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.INSTANCE.handleSignInResult(
                    this, data,
                    mRemoteBackupCallback
                ) { remoteBackup() }
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_RESTORE) {
            if (resultCode == RESULT_OK && data != null) {
                RemoteBackup.INSTANCE.handleSignInResult(
                    this, data,
                    mRemoteRestoreCallback
                ) { remoteRestore() }
            }
        } else if (requestCode == LocalBackup.REQUEST_CODE_PICK_FILE) {
            if (resultCode == RESULT_OK) {
                data?.data?.let {
                    showProgress("Start local restore...")
                    localRestore(it)
                }
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
        when {
            !RemoteBackup.INSTANCE.checkGooglePlayService(this) -> {
                menu.findItem(R.id.remote).isEnabled = false
            }
            RemoteBackup.INSTANCE.isGoogleAccountSignedIn(this) -> {
                menu.findItem(R.id.account).isVisible = true
                menu.findItem(R.id.account).title =
                    RemoteBackup.INSTANCE.getSignedInGoogleAccountEmail(this)
                menu.findItem(R.id.account_action).setTitle(R.string.switch_account)
                menu.findItem(R.id.remote_backup).isEnabled = true
                menu.findItem(R.id.remote_restore).isEnabled = true
                val avatar = RemoteBackup.INSTANCE.getSignedInGoogleAccountPhoto(this)
                val iconSize = 24f.dp2px().toInt()
                applicationContext.imageLoader.enqueue(
                    ImageRequest.Builder(applicationContext)
                        .data(avatar)
                        .size(iconSize)
                        .target(
                            onSuccess = { result ->
                                toolbar.menu?.let {
                                    it.findItem(R.id.account).icon = result
                                    invalidateOptionsMenu()
                                }
                            }
                        ).build())
            }
            else -> {
                menu.findItem(R.id.account_action).setTitle(R.string.connect_account)
                menu.findItem(R.id.account).isVisible = false
                menu.findItem(R.id.remote_backup).isEnabled = false
                menu.findItem(R.id.remote_restore).isEnabled = false
            }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    Uri.fromFile(File(File(LocalBackup.PREF_BACKUP_ROOT_DIR,
                        LocalBackup.PREF_BACKUP_RELATIVE_PATH),
                        QuoteCollectionContract.DATABASE_NAME)))
            }
        }

        startActivityForResult(intent, LocalBackup.REQUEST_CODE_PICK_FILE)
    }

    private fun localBackup() {
        LocalBackup.performBackup(
            this, QuoteCollectionContract.DATABASE_NAME,
            mLocalBackupCallback
        )
    }

    private fun localRestore(uri: Uri) {
        LocalBackup.performRestore(
            this, QuoteCollectionContract.DATABASE_NAME, uri, mLocalRestoreCallback
        )
    }

    private fun remoteBackup() {
        RemoteBackup.INSTANCE.performDriveBackupAsync(
            this,
            QuoteCollectionContract.DATABASE_NAME, mRemoteBackupCallback
        )
    }

    private fun remoteRestore() {
        RemoteBackup.INSTANCE.performDriveRestoreAsync(
            this,
            QuoteCollectionContract.DATABASE_NAME, mRemoteRestoreCallback
        )
    }

    private fun showProgress(message: String? = null) {
        val dialog = mLoadingDialog
            ?: (ProgressAlertDialog(this, message, true).also { mLoadingDialog = it })
        dialog.message = message
        dialog.show()
    }

    private fun hideProgress() {
        mLoadingDialog?.dismiss()
    }

    private abstract inner class AbstractBackupCallback : ProgressCallback {
        override fun inProcessing(message: String?) {
            showProgress(message)
        }

        override fun failure(message: String?) {
            Toast.makeText(
                applicationContext, message, Toast.LENGTH_SHORT
            ).show()
            hideProgress()
        }
    }
}