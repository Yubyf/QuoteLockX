package com.crossbowffs.quotelock.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crossbowffs.quotelock.app.settings.PreviewFragment
import com.crossbowffs.quotelock.app.settings.SettingsFragment
import com.crossbowffs.quotelock.components.ProgressAlertDialog
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.utils.WorkUtils.createQuoteDownloadWork
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.XposedUtils.isModuleEnabled
import com.crossbowffs.quotelock.utils.XposedUtils.isModuleUpdated
import com.crossbowffs.quotelock.utils.XposedUtils.startXposedActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var rootView: View
    private lateinit var mDialog: ProgressAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView = findViewById(R.id.root_view)

        // Toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setOnMenuItemClickListener {
            if (it.itemId == R.id.refesh_quote) {
                viewModel.refreshQuote()
            }
            true
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preview_frame, PreviewFragment())
            .replace(R.id.settings_frame, SettingsFragment())
            .commit()

        // In case the user opens the app for the first time *after* rebooting,
        // we want to make sure the background work has been created.
        createQuoteDownloadWork(this, false)
        if (savedInstanceState == null) {
            if (!isModuleEnabled) {
                showEnableModuleDialog()
            } else if (isModuleUpdated) {
                showModuleUpdatedDialog()
            }
        }
        checkBatteryOptimization()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect {
                    when (it) {
                        is MainUiEvent.SnackBarMessage -> {
                            it.message?.let { message ->
                                Snackbar.make(rootView, message, it.duration).show()
                            }
                        }
                        is MainUiEvent.ProgressMessage -> {
                            if (it.show) {
                                mDialog = ProgressAlertDialog(this@MainActivity, it.message,
                                    cancelable = true, canceledOnTouchOutside = false)
                                mDialog.show()
                            } else {
                                mDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshQuote() {
//        mainScope.launch {
//            mDialog =
//                ProgressAlertDialog(this@MainActivity, getString(R.string.downloading_quote),
//                    false)
//            mDialog.show()
//            val quote = try {
//                quoteRepository.downloadQuote()
//            } catch (e: CancellationException) {
//                null
//            }
//            mDialog.dismiss()
//            Snackbar.make(rootView,
//                if (quote == null) R.string.quote_download_failed else R.string.quote_download_success,
//                Snackbar.LENGTH_SHORT).show()
//        }
    }

    private fun startBrowserActivity(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun startXposedPage(section: String) {
        if (!this.startXposedActivity(section)) {
            Snackbar.make(rootView, R.string.xposed_not_installed, Snackbar.LENGTH_SHORT).show()
            startBrowserActivity(Urls.XPOSED_FORUM)
        }
    }

    private fun showEnableModuleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enable_xposed_module_title)
            .setMessage(R.string.enable_xposed_module_message)
            .setIcon(R.drawable.ic_round_warning_24dp)
            .setPositiveButton(R.string.enable) { _, _ -> startXposedPage(XposedUtils.XPOSED_SECTION_MODULES) }
            .setNeutralButton(R.string.report_bug) { _, _ -> startBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT_ISSUES) }
            .setNegativeButton(R.string.ignore, null)
            .show()
    }

    private fun showModuleUpdatedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.module_outdated_title)
            .setMessage(R.string.module_outdated_message)
            .setIcon(R.drawable.ic_round_warning_24dp)
            .setPositiveButton(R.string.reboot) { _, _ -> startXposedPage(XposedUtils.XPOSED_SECTION_INSTALL) }
            .setNegativeButton(R.string.ignore, null)
            .show()
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog()
            }
        }
    }

    @SuppressLint("BatteryLife")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun showBatteryOptimizationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.battery_optimization_title)
            .setMessage(R.string.battery_optimization_message)
            .setPositiveButton(R.string.disable) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}