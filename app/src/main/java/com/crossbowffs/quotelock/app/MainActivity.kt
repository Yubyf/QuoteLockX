package com.crossbowffs.quotelock.app

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.consts.Urls
import com.crossbowffs.quotelock.utils.WorkUtils.createQuoteDownloadWork
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.XposedUtils.isModuleEnabled
import com.crossbowffs.quotelock.utils.XposedUtils.isModuleUpdated
import com.crossbowffs.quotelock.utils.XposedUtils.startXposedActivity
import com.crossbowffs.quotelock.utils.mainScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preview_frame, PreviewFragment())
            .commit()

        supportFragmentManager
            .beginTransaction()
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refesh_quote -> {
                refreshQuote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshQuote() {
        mainScope.launch {
            mDialog = ProgressDialog(this@MainActivity)
            mDialog.setMessage(getString(R.string.downloading_quote))
            mDialog.isIndeterminate = true
            mDialog.setCancelable(false)
            mDialog.show()
            val quote = try {
                downloadQuote()
            } catch (e: CancellationException) {
                null
            }
            mDialog.dismiss()
            Toast.makeText(this@MainActivity,
                if (quote == null) R.string.quote_download_failed else R.string.quote_download_success,
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBrowserActivity(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun startXposedPage(section: String) {
        if (!this.startXposedActivity(section)) {
            Toast.makeText(this, R.string.xposed_not_installed, Toast.LENGTH_SHORT).show()
            startBrowserActivity(Urls.XPOSED_FORUM)
        }
    }

    private fun showEnableModuleDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.enable_xposed_module_title)
            .setMessage(R.string.enable_xposed_module_message)
            .setIcon(R.drawable.ic_baseline_warning_24dp)
            .setPositiveButton(R.string.enable) { _, _ -> startXposedPage(XposedUtils.XPOSED_SECTION_MODULES) }
            .setNeutralButton(R.string.report_bug) { _, _ -> startBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT_ISSUES) }
            .setNegativeButton(R.string.ignore, null)
            .show()
    }

    private fun showModuleUpdatedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.module_outdated_title)
            .setMessage(R.string.module_outdated_message)
            .setIcon(R.drawable.ic_baseline_warning_24dp)
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
        AlertDialog.Builder(this)
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