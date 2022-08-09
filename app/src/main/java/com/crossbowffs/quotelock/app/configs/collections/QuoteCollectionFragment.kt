package com.crossbowffs.quotelock.app.configs.collections

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.crossbowffs.quotelock.account.SyncAccountManager
import com.crossbowffs.quotelock.account.google.GoogleAccountHelper
import com.crossbowffs.quotelock.components.BaseQuoteListFragment
import com.crossbowffs.quotelock.components.ContextMenuRecyclerView
import com.crossbowffs.quotelock.components.ProgressAlertDialog
import com.crossbowffs.quotelock.components.QuoteListAdapter
import com.crossbowffs.quotelock.data.modules.collections.database.QuoteCollectionEntity
import com.crossbowffs.quotelock.utils.dp2px
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Yubyf
 */
@AndroidEntryPoint
class QuoteCollectionFragment : BaseQuoteListFragment<QuoteCollectionEntity>(), MenuProvider {

    private val viewModel: QuoteCollectionViewModel by viewModels()

    @Inject
    lateinit var syncAccountManager: SyncAccountManager

    private var menu: Menu? = null

    private var collectionsObserver: Job? = null

    private var mLoadingDialog: ProgressAlertDialog? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (!result) {
                showSnackbar(getString(R.string.grant_storage_permission_tips))
            }
        }

    private val pickDbFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let {
                    viewModel.import(LocalBackupType.DB, it)
                }
            }
        }

    private val pickCsvFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let {
                    viewModel.import(LocalBackupType.CSV, it)
                }
            }
        }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                GoogleAccountHelper.handleSignInResult(result.data) { success ->
                    if (success) {
                        viewModel.updateDriveService()
                        showSnackbar(R.string.google_account_connected)
                        initMenu(menu)
                        requireActivity().invalidateOptionsMenu()
                        val accountName =
                            GoogleAccountHelper.getSignedInGoogleAccountEmail(requireContext())
                        if (!accountName.isNullOrEmpty()) {
                            syncAccountManager.addOrUpdateAccount(accountName)
                        }
                    } else {
                        showSnackbar(R.string.google_account_sign_in_failed)
                    }
                }
            }
        }

    private val googleBackupLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                GoogleAccountHelper.handleSignInResult(result.data) { success ->
                    if (success) {
                        viewModel.gDriveBackup()
                    } else {
                        hideProgress()
                        showSnackbar(R.string.google_account_sign_in_failed)
                    }
                }
            }
        }

    private val googleRestoreLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                GoogleAccountHelper.handleSignInResult(result.data) { success ->
                    if (success) {
                        viewModel.gDriveRestore()
                    } else {
                        hideProgress()
                        showSnackbar(R.string.google_account_sign_in_failed)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setFragmentResult(REQUEST_KEY_COLLECTION_LIST_PAGE,
            bundleOf(BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE to false))
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return super.onCreateView(inflater, container, savedInstanceState).also {
            observeCollections()
        }
    }

    override fun onDetach() {
        hideProgress()
        mLoadingDialog = null
        super.onDetach()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.custom_quote_context, menu)
        menu.findItem(R.id.edit_quote).isVisible = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? ContextMenuRecyclerView.ContextMenuInfo
        if (item.itemId == R.id.delete_quote) {
            info?.let {
                viewModel.delete(it.id)
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun showDetailPage(): Boolean = true

    override fun goToDetailPage() {
        setFragmentResult(REQUEST_KEY_COLLECTION_LIST_PAGE,
            bundleOf(BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE to true))
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.collections_options, menu)
        initMenu(menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        this.menu = menu
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.account_action -> {
                if (GoogleAccountHelper.isGoogleAccountSignedIn(requireActivity())) {
                    showProgress(getString(R.string.sign_out_google_account))
                    GoogleAccountHelper.signOutAccount(requireActivity()) { success, message ->
                        hideProgress()
                        if (success) {
                            initMenu(menu)
                            requireActivity().invalidateOptionsMenu()
                            if (message.isNotEmpty()) {
                                syncAccountManager.removeAccount(message)
                            }
                        } else {
                            showSnackbar(message)
                        }
                    }
                } else {
                    GoogleAccountHelper.requestSignIn(requireActivity(),
                        googleSignInLauncher)
                }
            }
            R.id.export_database -> {
                export(LocalBackupType.DB)
                return true
            }
            R.id.export_csv -> {
                export(LocalBackupType.CSV)
                return true
            }
            R.id.import_database -> {
                pickFile(LocalBackupType.DB)
                return true
            }
            R.id.import_csv -> {
                pickFile(LocalBackupType.CSV)
                return true
            }
            R.id.remote_backup -> {
                checkGoogleAccount(requireActivity(), googleBackupLauncher)
                    .takeIf { it }.run {
                        viewModel.gDriveBackup()
                    }
                return true
            }
            R.id.remote_restore -> {
                checkGoogleAccount(requireActivity(), googleRestoreLauncher)
                    .takeIf { it }.run {
                        viewModel.gDriveRestore()
                    }
                return true
            }
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeCollections() {
        collectionsObserver?.cancel()
        collectionsObserver = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiListState.onEach { (items, exportEnabled) ->
                    (recyclerView.adapter as? QuoteListAdapter<QuoteCollectionEntity>)?.submitList(
                        items)
                    scrollToPosition()
                    menu?.findItem(R.id.export_database)?.isEnabled = exportEnabled
                    menu?.findItem(R.id.export_csv)?.isEnabled = exportEnabled
                }.launchIn(this)
                viewModel.uiEvent.onEach {
                    when (it) {
                        is QuoteCollectionUiEvent.SnackBarMessage -> {
                            it.message?.let { message ->
                                showSnackbar(message, it.duration, it.actionText)
                            }
                        }
                        is QuoteCollectionUiEvent.ProgressMessage -> {
                            if (it.show) {
                                showProgress(it.message)
                            } else {
                                hideProgress()
                            }
                        }
                    }
                }.launchIn(this)
            }
        }
    }

    private fun initMenu(menu: Menu?) {
        if (menu == null) {
            return
        }
        when {
            !GoogleAccountHelper.checkGooglePlayService(requireContext()) -> {
                menu.findItem(R.id.sync).isEnabled = false
            }
            GoogleAccountHelper.isGoogleAccountSignedIn(requireContext()) -> {
                menu.findItem(R.id.account).isVisible = true
                menu.findItem(R.id.account).title =
                    GoogleAccountHelper.getSignedInGoogleAccountEmail(requireContext())
                menu.findItem(R.id.account_action).setTitle(R.string.disconnect_account)
                menu.findItem(R.id.remote_backup).isEnabled = true
                menu.findItem(R.id.remote_restore).isEnabled = true
                val avatar = GoogleAccountHelper.getSignedInGoogleAccountPhoto(requireContext())
                val iconSize = 24f.dp2px().toInt()
                requireContext().applicationContext.imageLoader.enqueue(
                    ImageRequest.Builder(requireContext().applicationContext)
                        .data(avatar)
                        .size(iconSize)
                        .target(
                            onSuccess = { result ->
                                this.menu?.let {
                                    it.findItem(R.id.account).icon = result
                                    requireActivity().invalidateOptionsMenu()
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
        menu.findItem(R.id.export_database)?.isEnabled = viewModel.uiListState.value.exportEnabled
        menu.findItem(R.id.export_csv)?.isEnabled = viewModel.uiListState.value.exportEnabled
    }

    private fun pickFile(@LocalBackupType importType: Int = LocalBackupType.DB) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        if (importType == LocalBackupType.CSV) {
            pickCsvFileLauncher.launch(intent)
        } else {
            pickDbFileLauncher.launch(intent)
        }
    }

    private fun export(@LocalBackupType type: Int = LocalBackupType.DB) {
        if (!ensurePermissions()) {
            return
        }
        viewModel.export(type)
    }

    private fun checkGoogleAccount(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean {
        return if (!viewModel.ensureDriveService()) {
            GoogleAccountHelper.requestSignIn(activity, resultLauncher)
            false
        } else true
    }

    private fun showProgress(message: String? = null) {
        val dialog = mLoadingDialog
            ?: (ProgressAlertDialog(requireActivity(), message, true).also { mLoadingDialog = it })
        dialog.message = message
        dialog.show()
    }

    private fun hideProgress() {
        mLoadingDialog?.dismiss()
    }

    private fun showSnackbar(
        @StringRes message: Int,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null,
    ) {
        showSnackbar(getString(message), duration, actionText, action)
    }

    private fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null,
    ) {
        Snackbar.make(requireView(), message, duration).apply {
            // show multiple line
            (view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as? TextView)
                ?.maxLines = 5
            if (!actionText.isNullOrBlank()) {
                setAction(actionText) {
                    action?.invoke()
                }
            }
        }.show()
    }

    private fun ensurePermissions(): Boolean =
        // Use MediaStore to save files in public directories above Android Q.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !verifyPermissions()
        ) {
            showSnackbar(R.string.grant_storage_permission_tips)
            false
        } else true

    /** Check necessary permissions. */
    private fun verifyPermissions(): Boolean {
        // Check if we have write permission
        return if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permission so prompt the user
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            false
        } else true
    }

    companion object {
        const val REQUEST_KEY_COLLECTION_LIST_PAGE = "collection_list_page"
        const val BUNDLE_KEY_COLLECTION_SHOW_DETAIL_PAGE = "collection_show_detail_page"
        private const val REQUEST_CODE_PERMISSIONS_EXPORT = 55
    }
}