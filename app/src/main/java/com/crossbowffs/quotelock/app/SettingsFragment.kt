package com.crossbowffs.quotelock.app

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.crossbowffs.quotelock.BuildConfig
import com.crossbowffs.quotelock.R
import com.crossbowffs.quotelock.api.QuoteModule
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.commonDataStore
import com.crossbowffs.quotelock.data.quotesDataStore
import com.crossbowffs.quotelock.history.app.QuoteHistoryActivity
import com.crossbowffs.quotelock.modules.ModuleManager
import com.crossbowffs.quotelock.modules.ModuleNotFoundException
import com.crossbowffs.quotelock.utils.WorkUtils
import com.crossbowffs.quotelock.utils.XposedUtils
import com.crossbowffs.quotelock.utils.className
import com.crossbowffs.quotelock.utils.ioScope
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
            PREF_ABOUT_GITHUB -> {
                startBrowserActivity(Urls.GITHUB_QUOTELOCK)
                true
            }
            PREF_ABOUT_GITHUB_CURRENT -> {
                startBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT)
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
            Toast.makeText(requireContext(), R.string.selected_module_not_found, Toast.LENGTH_SHORT)
                .show()
            loadSelectedModule(prefs)
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (PREF_COMMON_FONT_FAMILY == preference.key) {
            showFontFamilyDialog(preference)
            return
        }
        super.onDisplayPreferenceDialog(preference)
    }

    private fun showFontFamilyDialog(preference: Preference) {
        // check if dialog is already showing
        var dialogFragmentTag: String? = null
        try {
            val clazz: Class<*> = javaClass.superclass
            val field = clazz.getDeclaredField("DIALOG_FRAGMENT_TAG")
            field.isAccessible = true
            val tag = field[this]
            if (tag is String) {
                dialogFragmentTag = tag
            }
        } catch (e: NoSuchFieldException) {
            dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"
            e.printStackTrace()
        }
        if (requireFragmentManager().findFragmentByTag(dialogFragmentTag) != null) {
            return
        }
        val f: DialogFragment
        f = FontListPreferenceDialogFragmentCompat.newInstance(preference.key)
        f.setTargetFragment(this, 0)
        f.show(requireFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG")
    }

    private fun showCreditsDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.credits_title)
            .setMessage(Html.fromHtml(getString(R.string.credits_message)))
            .setPositiveButton(R.string.close, null)
            .show()
        val textView = dialog.findViewById<TextView>(android.R.id.message)
        textView.movementMethod = LinkMovementMethod.getInstance()
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

        // Update font family options
        val fontFamilyPref = findPreference<ListPreference>(PREF_COMMON_FONT_FAMILY)
        var entries = R.array.font_family_entries
        var values = R.array.font_family_values
        if (QuoteModule.CHARACTER_TYPE_LATIN == module.characterType) {
            entries = R.array.font_family_latin_entries
            values = R.array.font_family_latin_values
        } else if (QuoteModule.CHARACTER_TYPE_CJK == module.characterType) {
            entries = R.array.font_family_cjk_entries
            values = R.array.font_family_cjk_values
        }
        fontFamilyPref?.run {
            setEntries(entries)
            setEntryValues(values)
            if (findIndexOfValue(value) < 0) {
                setValueIndex(0)
            }
        }
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