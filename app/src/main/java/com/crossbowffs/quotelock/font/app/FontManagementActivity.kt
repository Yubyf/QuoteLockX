package com.crossbowffs.quotelock.font.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.crossbowffs.quotelock.components.ProgressAlertDialog
import com.crossbowffs.quotelock.consts.Urls.GITHUB_QUOTELOCK_CUSTOM_FONTS_RELEASE
import com.crossbowffs.quotelock.font.FontManager
import com.crossbowffs.quotelock.font.FontManager.REQUEST_CODE_PICK_FONT
import com.crossbowffs.quotelock.utils.className
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author Yubyf
 */
class FontManagementActivity : AppCompatActivity() {

    private lateinit var container: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fab: ExtendedFloatingActionButton
    private var loadingDialog: ProgressAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        container = findViewById(R.id.content_frame)

        // Toolbar
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.quote_fonts_management_activity_label)
            setNavigationIcon(R.drawable.ic_round_close_24dp)
            setNavigationOnClickListener { onBackPressed() }
        }

        if (FontManager.checkSystemCustomFontAvailable()) {
            fab = findViewById<ExtendedFloatingActionButton>(R.id.fab).apply {
                visibility = View.VISIBLE
                setIconResource(R.drawable.ic_round_add_24dp)
                setText(R.string.quote_fonts_management_import)
                setOnClickListener {
                    pickFontFile()
                }
            }
            supportFragmentManager.apply {
                beginTransaction()
                    .add(R.id.content_frame, FontManagementFragment())
                    .commit()
            }
        } else {
            showEnableMagiskModuleDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FONT && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                showProgress()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = FontManager.importFont(this@FontManagementActivity, uri)
                        if (!result.isNullOrEmpty()) {
                            FontManager.loadFontInfo(File(result))
                        } else {
                            null
                        }
                    }?.let {
                        if (FontManager.isFontActivated(it.fileName)) {
                            FontManager.deleteInactiveFont(it.fileName)
                            getString(R.string.quote_fonts_management_font_already_exists, it.name)
                        } else {
                            supportFragmentManager.findFragmentById(R.id.content_frame)
                                ?.let { fragment ->
                                    (fragment as FontManagementFragment).onFontListChanged()
                                }
                            getString(R.string.quote_fonts_management_font_imported, it.name)
                        }.let { message ->
                            Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(container,
                                R.string.quote_fonts_management_import_failed,
                                Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    withContext(Dispatchers.Main) { hideProgress() }
                }
            }
        }
    }

    private fun pickFontFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("font/ttf", "font/otf"))
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_FONT)
    }

    private fun showEnableMagiskModuleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.quote_fonts_magisk_module_needed_title)
            .setMessage(R.string.quote_fonts_magisk_module_needed_message)
            .setIcon(R.drawable.ic_round_warning_24dp)
            .setNegativeButton(R.string.ignore) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.download) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(GITHUB_QUOTELOCK_CUSTOM_FONTS_RELEASE)))
            }
            .setOnDismissListener { onBackPressed() }
            .show()
    }

    private fun showProgress(message: String? = null) {
        val dialog = loadingDialog
            ?: (ProgressAlertDialog(this, message, true).also { loadingDialog = it })
        dialog.message = message
        dialog.show()
    }

    private fun hideProgress() {
        loadingDialog?.dismiss()
    }

    companion object {
        private val TAG = className<FontManagementActivity>()
    }
}