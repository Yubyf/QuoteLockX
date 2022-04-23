package com.crossbowffs.quotelock.app

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity
import com.crossbowffs.quotelock.components.MaterialListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.components.MaterialMultiSelectListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.commonDataStore
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.font.FontListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.font.FontManager
import com.crossbowffs.quotelock.history.app.QuoteHistoryActivity
import com.crossbowffs.quotelock.modules.ModuleManager
import com.crossbowffs.quotelock.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.utils.*
import com.crossbowffs.quotelock.xposed.LockscreenHook
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private var mVersionTapCount = 0
    private var mModuleConfigActivity: ComponentName? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = commonDataStore
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Only enable DisplayOnAOD on tested devices.
        if (!XposedUtils.isAodHookAvailable) {
            findPreference<Preference>(PREF_COMMON_DISPLAY_ON_AOD)?.isVisible = false
        }

        // Only enable font family above API26
        findPreference<Preference>(PREF_COMMON_FONT_FAMILY)?.isEnabled =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        // Update version info
        findPreference<Preference>(PREF_ABOUT_VERSION)?.summary =
            "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"

        // Last update info
        val lastUpdate = quotesDataStore.getLong(PREF_QUOTES_LAST_UPDATED, -1)
        findPreference<Preference>(PREF_COMMON_UPDATE_INFO)?.summary =
            getString(R.string.pref_refresh_info_summary,
                if (lastUpdate > 0) DATE_FORMATTER.format(Date(
                    lastUpdate)) else "-")

        // Get quote module list
        val quoteModules = ModuleManager.getAllModules(requireContext())
        val moduleNames = arrayOfNulls<String>(quoteModules.size)
        val moduleClsNames = arrayOfNulls<String>(quoteModules.size)
        for (i in moduleNames.indices) {
            val module = quoteModules[i]
            moduleNames[i] = module.getDisplayName(requireContext())
            moduleClsNames[i] = module::class.qualifiedName
        }

        // Update quote module list
        findPreference<ListPreference>(PREF_COMMON_QUOTE_MODULE)?.run {
            entries = moduleNames
            entryValues = moduleClsNames
        }

        // Update preferences related to module
        onSelectedModuleChanged()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    quotesDataStore.collectSuspend { preferences, key ->
                        if (key?.name != PREF_QUOTES_LAST_UPDATED) {
                            return@collectSuspend
                        }
                        preferences[longPreferencesKey(PREF_QUOTES_LAST_UPDATED)]?.run {
                            findPreference<Preference>(PREF_COMMON_UPDATE_INFO)?.summary =
                                getString(R.string.pref_refresh_info_summary,
                                    if (this > 0) DATE_FORMATTER.format(Date(
                                        this)) else "-")
                        }
                    }
                }
                launch {
                    commonDataStore.collectSuspend { _, key ->
                        when (key?.name) {
                            PREF_COMMON_QUOTE_MODULE -> onSelectedModuleChanged()
                            PREF_COMMON_REFRESH_RATE, PREF_COMMON_REFRESH_RATE_OVERRIDE ->
                                WorkUtils.createQuoteDownloadWork(requireContext(), true)
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            PREF_COMMON_MODULE_PREFERENCES -> {
                startActivity(mModuleConfigActivity)
                true
            }
            PREF_FEATURES_COLLECTION -> {
                startActivity(ComponentName(requireContext(), QuoteCollectionActivity::class.java))
                true
            }
            PREF_FEATURES_HISTORY -> {
                startActivity(ComponentName(requireContext(), QuoteHistoryActivity::class.java))
                true
            }
            PREF_ABOUT_CREDITS -> {
                showCreditsDialog()
                true
            }
            PREF_DEBUG_RESTART_SYSTEM_UI -> {
                findProcessAndKill(LockscreenHook.PACKAGE_SYSTEM_UI)
                true
            }
            PREF_ABOUT_GITHUB -> {
                startBrowserActivity(Urls.GITHUB_QUOTELOCK)
                true
            }
            PREF_ABOUT_VERSION -> {
                if (++mVersionTapCount == 7) {
                    mVersionTapCount = 0
                    Toast.makeText(requireContext(), R.string.easter_egg, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun loadSelectedModule(prefs: PreferenceDataStore): QuoteModule {
        val moduleClsName =
            prefs.getString(PREF_COMMON_QUOTE_MODULE, PREF_COMMON_QUOTE_MODULE_DEFAULT)
        return try {
            ModuleManager.getModule(requireContext(), moduleClsName ?: "")
        } catch (e: ModuleNotFoundException) {
            // Reset to the default module if the currently
            // selected one was not found. Change through the
            // ListPreference so that it updates its value.
            findPreference<ListPreference>(PREF_COMMON_QUOTE_MODULE)?.value =
                PREF_COMMON_QUOTE_MODULE_DEFAULT
            Snackbar.make(requireView(), R.string.selected_module_not_found, Snackbar.LENGTH_SHORT)
                .show()
            loadSelectedModule(prefs)
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (!showPreferenceDialog(preference) {
                when (val key = it.key) {
                    PREF_COMMON_QUOTE_MODULE,
                    PREF_COMMON_REFRESH_RATE,
                    PREF_COMMON_FONT_SIZE_TEXT,
                    PREF_COMMON_FONT_SIZE_SOURCE,
                    PREF_COMMON_QUOTE_SPACING,
                    PREF_COMMON_PADDING_TOP,
                    PREF_COMMON_PADDING_BOTTOM,
                    -> MaterialListPreferenceDialogFragmentCompat.newInstance(key)
                    PREF_COMMON_FONT_STYLE_TEXT,
                    PREF_COMMON_FONT_STYLE_SOURCE,
                    -> MaterialMultiSelectListPreferenceDialogFragmentCompat.newInstance(key)
                    PREF_COMMON_FONT_FAMILY -> {
                        findPreference<ListPreference>(PREF_COMMON_FONT_FAMILY)?.run {
                            FontManager.loadActiveFontFilesList()?.let { fontList ->
                                val entries =
                                    arrayOfNulls<CharSequence>(fontList.size + 1)
                                val entryValues =
                                    arrayOfNulls<CharSequence>(entries.size)
                                entries[0] = "System"
                                entryValues[0] = PREF_COMMON_FONT_FAMILY_DEFAULT
                                fontList.forEachIndexed { index, fontInfo ->
                                    entries[index + 1] = fontInfo.nameWithoutExtension
                                    entryValues[index + 1] = fontInfo.path
                                }
                                setEntries(entries)
                                setEntryValues(entryValues)
                                if (findIndexOfValue(value) < 0) {
                                    setValueIndex(0)
                                }
                            }
                        }
                        FontListPreferenceDialogFragmentCompat.newInstance(key)
                    }
                    else -> null
                }
            }
        ) super.onDisplayPreferenceDialog(preference)
    }

    private fun showPreferenceDialog(
        preference: Preference, dialogBlock: ((Preference) -> DialogFragment?),
    ): Boolean {
        // check if dialog is already showing
        val dialogTag = runCatching {
            getReflectionField<String>("DIALOG_FRAGMENT_TAG1")
        }.getOrNull()
            ?: "androidx.preference.PreferenceFragment.DIALOG"
        if (parentFragmentManager.findFragmentByTag(dialogTag) != null) {
            return false
        }
        dialogBlock.invoke(preference)?.let {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, dialogTag)
            return true
        } ?: return false
    }

    private fun showCreditsDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.credits_title)
            .setMessage(HtmlCompat.fromHtml(getString(R.string.credits_message),
                FROM_HTML_MODE_COMPACT))
            .setPositiveButton(R.string.close, null)
            .setBackgroundInsetTop(0)
            .setBackgroundInsetBottom(0)
            .show()
        val textView = dialog.findViewById<TextView>(android.R.id.message)
        textView?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onSelectedModuleChanged() {
        val module = loadSelectedModule(commonDataStore)

        // Update config activity preference
        val configActivity = module.getConfigActivity(requireContext())
        val configActivityPref = findPreference<Preference>(PREF_COMMON_MODULE_PREFERENCES)
        if (configActivity == null) {
            configActivityPref?.isEnabled = false
            configActivityPref?.summary = getString(R.string.pref_module_preferences_summary_alt)
            mModuleConfigActivity = null
        } else {
            configActivityPref?.isEnabled = true
            configActivityPref?.summary = getString(R.string.pref_module_preferences_summary)
            mModuleConfigActivity = configActivity
        }

        // Set refresh interval override and disable preference if necessary.
        // This is kind of a lazy solution, but it's better than nothing.
        val minRefreshInterval = module.getMinimumRefreshInterval(requireContext())
        val refreshIntervalPref = findPreference<Preference>(PREF_COMMON_REFRESH_RATE)
        if (minRefreshInterval != 0) {
            commonDataStore.putInt(PREF_COMMON_REFRESH_RATE_OVERRIDE, minRefreshInterval)
            refreshIntervalPref?.isEnabled = false
            refreshIntervalPref?.summary = getString(R.string.pref_refresh_interval_summary_alt)
        } else {
            commonDataStore.remove(PREF_COMMON_REFRESH_RATE_OVERRIDE)
            refreshIntervalPref?.isEnabled = true
            refreshIntervalPref?.summary = getString(R.string.pref_refresh_interval_summary)
        }

        // If the module doesn't require internet connectivity, disable the
        // unmetered only toggle and set the requires internet preference to false.
        val requiresInternet = module.requiresInternetConnectivity(requireContext())
        val unmeteredOnlyPref = findPreference<Preference>(PREF_COMMON_UNMETERED_ONLY)
        if (!requiresInternet) {
            commonDataStore.putBoolean(PREF_COMMON_REQUIRES_INTERNET, false)
            unmeteredOnlyPref?.isEnabled = false
            unmeteredOnlyPref?.summary = getString(R.string.pref_unmetered_only_summary_alt)
        } else {
            commonDataStore.remove(PREF_COMMON_REQUIRES_INTERNET)
            unmeteredOnlyPref?.isEnabled = true
            unmeteredOnlyPref?.summary = getString(R.string.pref_unmetered_only_summary)
        }

        // Update quotes.
        requireContext().run { ioScope.launch { downloadQuote() } }
    }

    private fun startActivity(componentName: ComponentName?) {
        if (componentName != null) {
            val intent = Intent()
            intent.component = componentName
            startActivity(intent)
        }
    }

    private fun startBrowserActivity(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    companion object {
        private val TAG = className<SettingsFragment>()
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}