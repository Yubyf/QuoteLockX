package com.crossbowffs.quotelock.collections.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.view.MenuCompat
import coil.imageLoader
import coil.request.ImageRequest
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.backup.ExportHelper
import com.crossbowffs.quotelock.backup.GDriveSyncManager
import com.crossbowffs.quotelock.backup.ImportExportType
import com.crossbowffs.quotelock.collections.database.QuoteCollectionContract
import com.crossbowffs.quotelock.components.ProgressAlertDialog
import com.crossbowffs.quotelock.utils.dp2px
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import java.io.File


/**
 * @author Yubyf
 */
class QuoteCollectionActivity : AppCompatActivity() {

    private lateinit var container: View
    private lateinit var toolbar: MaterialToolbar
    private var mLoadingDialog: ProgressAlertDialog? = null

    private val menuItemClickListener: OnMenuItemClickListener = object : OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if (item == null) {
                return false
            }
            when (item.itemId) {
                R.id.account_action -> {
                    if (GDriveSyncManager.INSTANCE.isGoogleAccountSignedIn(this@QuoteCollectionActivity)) {
                        showProgress(getString(R.string.sign_out_google_account))
                        GDriveSyncManager.INSTANCE.signOutAccount(this@QuoteCollectionActivity)
                        { success, message ->
                            if (success) {
                                hideProgress()
                                initMenu(toolbar.menu)
                                invalidateOptionsMenu()
                                if (message.isNotEmpty()) {
                                    SyncAccountManager.instance.removeAccount(message)
                                }
                            } else {
                                showTerminateMessage(message)
                            }
                        }
                    } else {
                        GDriveSyncManager.INSTANCE.requestSignIn(this@QuoteCollectionActivity,
                            GDriveSyncManager.REQUEST_CODE_SIGN_IN)
                    }
                }
                R.id.export_database -> {
                    showProgress(getString(R.string.exporting_database))
                    export(ImportExportType.DB)
                    return true
                }
                R.id.export_csv -> {
                    showProgress(getString(R.string.exporting_csv))
                    export(ImportExportType.CSV)
                    return true
                }
                R.id.import_database -> {
                    pickFile(ImportExportType.DB)
                    return true
                }
                R.id.import_csv -> {
                    pickFile(ImportExportType.CSV)
                    return true
                }
                R.id.remote_backup -> {
                    showProgress(getString(R.string.start_google_drive_backup))
                    remoteBackup()
                    return true
                }
                R.id.remote_restore -> {
                    showProgress(getString(R.string.start_google_drive_restore))
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
        container = findViewById(R.id.content_frame)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_collections_activity_label)
            setNavigationIcon(R.drawable.ic_round_arrow_back_24dp)
            inflateMenu(R.menu.collections_options)
            initMenu(menu)
            MenuCompat.setGroupDividerEnabled(menu, true);
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
                toolbar.menu?.findItem(R.id.data_retention)?.isVisible = !result
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ExportHelper.REQUEST_CODE_PERMISSIONS_EXPORT) {
            ExportHelper.handleRequestPermissionsResult(grantResults, ::showTerminateMessage)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GDriveSyncManager.REQUEST_CODE_SIGN_IN -> {
                if (resultCode == RESULT_OK && data != null) {
                    GDriveSyncManager.INSTANCE.handleSignInResult(
                        this, data
                    ) { success, message ->
                        if (success) {
                            showTerminateMessage(R.string.google_account_connected)
                            initMenu(toolbar.menu)
                            invalidateOptionsMenu()
                            val accountName =
                                GDriveSyncManager.INSTANCE.getSignedInGoogleAccountEmail(this)
                            if (!accountName.isNullOrEmpty()) {
                                SyncAccountManager.instance.addOrUpdateAccount(accountName)
                            }
                        } else {
                            showTerminateMessage(message)
                        }
                    }
                }
            }
            GDriveSyncManager.REQUEST_CODE_SIGN_IN_BACKUP -> {
                if (resultCode == RESULT_OK && data != null) {
                    GDriveSyncManager.INSTANCE.handleSignInResult(
                        this, data,
                    ) { success, message ->
                        if (success) {
                            remoteBackup()
                        } else {
                            showTerminateMessage(message)
                        }
                    }
                }
            }
            GDriveSyncManager.REQUEST_CODE_SIGN_IN_RESTORE -> {
                if (resultCode == RESULT_OK && data != null) {
                    GDriveSyncManager.INSTANCE.handleSignInResult(
                        this, data,
                    ) { success, message ->
                        if (success) {
                            remoteRestore()
                        } else {
                            showTerminateMessage(message)
                        }
                    }
                }
            }
            ExportHelper.REQUEST_CODE_PICK_DB_FILE -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let {
                        showProgress(getString(R.string.importing_database))
                        import(ImportExportType.DB, it)
                    }
                }
            }
            ExportHelper.REQUEST_CODE_PICK_CSV_FILE -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let {
                        showProgress(getString(R.string.importing_csv))
                        import(ImportExportType.CSV, it)
                    }
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
            !GDriveSyncManager.INSTANCE.checkGooglePlayService(this) -> {
                menu.findItem(R.id.sync).isEnabled = false
            }
            GDriveSyncManager.INSTANCE.isGoogleAccountSignedIn(this) -> {
                menu.findItem(R.id.account).isVisible = true
                menu.findItem(R.id.account).title =
                    GDriveSyncManager.INSTANCE.getSignedInGoogleAccountEmail(this)
                menu.findItem(R.id.account_action).setTitle(R.string.disconnect_account)
                menu.findItem(R.id.remote_backup).isEnabled = true
                menu.findItem(R.id.remote_restore).isEnabled = true
                val avatar = GDriveSyncManager.INSTANCE.getSignedInGoogleAccountPhoto(this)
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

    private fun pickFile(@ImportExportType importType: Int = ImportExportType.DB) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    Uri.fromFile(File(ExportHelper.PREF_EXPORT_ROOT_DIR,
                        ExportHelper.PREF_EXPORT_RELATIVE_PATH)))
            }
        }

        startActivityForResult(intent,
            if (importType == ImportExportType.CSV) ExportHelper.REQUEST_CODE_PICK_CSV_FILE
            else ExportHelper.REQUEST_CODE_PICK_DB_FILE)
    }

    private fun export(@ImportExportType exportType: Int = ImportExportType.DB) {
        ExportHelper.performExport(
            this, QuoteCollectionContract.DATABASE_NAME, exportType
        ) { success, message ->
            if (success) {
                showTerminateMessage(message = getString(if (exportType == ImportExportType.CSV) {
                    R.string.csv_exported
                } else {
                    R.string.database_exported
                }).plus(" ").plus(message),
                    duration = Snackbar.LENGTH_LONG,
                    actionText = getString(R.string.ok))
            } else {
                showTerminateMessage(message)
            }
        }
    }

    private fun import(@ImportExportType importType: Int, uri: Uri) {
        ExportHelper.performImport(
            this, QuoteCollectionContract.DATABASE_NAME, uri, importType
        ) { success, message ->
            showTerminateMessage(if (success) {
                getString(if (importType == ImportExportType.CSV) R.string.csv_imported
                else R.string.database_imported)
            } else {
                message
            })
        }
    }

    private fun remoteBackup() {
        GDriveSyncManager.INSTANCE.performDriveBackupAsync(this,
            QuoteCollectionContract.DATABASE_NAME,
            ::showProgress
        ) { success, message ->
            showTerminateMessage(if (success) {
                getString(R.string.remote_backup_completed)
            } else {
                message
            })
        }
    }

    private fun remoteRestore() {
        GDriveSyncManager.INSTANCE.performDriveRestoreAsync(
            this,
            QuoteCollectionContract.DATABASE_NAME,
            ::showProgress
        ) { success, message ->
            showTerminateMessage(if (success) {
                getString(R.string.remote_restore_completed)
            } else {
                message
            })
        }
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

    private fun showTerminateMessage(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null,
    ) {
        Snackbar.make(container, message, duration).apply {
            // show multiple line
            (view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as? TextView)
                ?.maxLines = 5
            if (!actionText.isNullOrBlank()) {
                setAction(actionText) {
                    action?.invoke()
                }
            }
        }.show()
        hideProgress()
    }

    private fun showTerminateMessage(
        messageRes: Int,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: Int? = null,
        action: (() -> Unit)? = null,
    ) {
        Snackbar.make(container, messageRes, duration).apply {
            // show multiple line
            (view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as? TextView)
                ?.maxLines = 5
            actionText?.let {
                setAction(it) {
                    action?.invoke()
                }
            }
        }.show()
        hideProgress()
    }
}