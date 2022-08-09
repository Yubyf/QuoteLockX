package com.crossbowffs.quotelock.app.settings

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.crossbowffs.quotelock.app.configs.collections.QuoteCollectionActivity
import com.crossbowffs.quotelock.app.font.FontListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.app.font.FontManager
import com.crossbowffs.quotelock.app.history.QuoteHistoryActivity
import com.crossbowffs.quotelock.components.MaterialListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.components.MaterialMultiSelectListPreferenceDialogFragmentCompat
import com.crossbowffs.quotelock.consts.*
import com.crossbowffs.quotelock.data.api.QuoteModuleData
import com.crossbowffs.quotelock.data.datastore.PreferenceDataStoreAdapter
import com.crossbowffs.quotelock.di.CommonDataStore
import com.crossbowffs.quotelock.utils.*
import com.crossbowffs.quotelock.xposed.LockscreenHook
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yubyf.quotelockx.BuildConfig
import com.yubyf.quotelockx.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    @CommonDataStore
    lateinit var commonDataStore: PreferenceDataStoreAdapter

    private var versionTapCount = 0

    private var quoteModuleData: QuoteModuleData? = null

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

        val moduleList = viewModel.getModuleList()

        // Update quote module list
        findPreference<ListPreference>(PREF_COMMON_QUOTE_MODULE)?.run {
            entries = moduleList.first
            entryValues = moduleList.second
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.onEach {
                        it.moduleData.takeIf { module -> module != quoteModuleData }
                            ?.let { module ->
                                onSelectedModuleChanged(module)
                            }
                        // Last update info
                        it.updateInfo.let { updateInfo ->
                            findPreference<Preference>(PREF_COMMON_UPDATE_INFO)?.summary =
                                updateInfo
                        }
                    }.launchIn(this)
                    viewModel.uiEvent.onEach { event ->
                        when (event) {
                            is SettingsUiEvent.SnackBarMessage -> {
                                event.message?.let {
                                    Snackbar.make(
                                        requireView(),
                                        event.message,
                                        event.duration,
                                    ).show()
                                }
                            }
                            is SettingsUiEvent.ProgressMessage -> {}
                            is SettingsUiEvent.SelectModule -> {
                                findPreference<ListPreference>(PREF_COMMON_QUOTE_MODULE)?.value =
                                    event.module
                            }
                            is SettingsUiEvent.StartWorker -> {
                                WorkUtils.createQuoteDownloadWork(requireContext(), true)
                            }
                        }
                    }.launchIn(this)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update preferences related to module
        viewModel.refreshSelectedModule()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            PREF_COMMON_MODULE_PREFERENCES -> {
                startActivity(quoteModuleData?.configActivity)
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
                if (++versionTapCount == 7) {
                    versionTapCount = 0
                    Toast.makeText(requireContext(), R.string.easter_egg, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onPreferenceTreeClick(preference)
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
                                getEntries()?.let { defaultEntries ->
                                    entries[0] = defaultEntries[0]
                                }
                                getEntryValues()?.let { defaultEntryValues ->
                                    entryValues[0] = defaultEntryValues[0]
                                }
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
            getReflectionField<String>("DIALOG_FRAGMENT_TAG")
        }.getOrNull() ?: "androidx.preference.PreferenceFragment.DIALOG"
        if (parentFragmentManager.findFragmentByTag(dialogTag) != null) {
            return false
        }
        return dialogBlock.invoke(preference)?.let {
            // PreferenceDialogFragmentCompat still uses deprecated getTargetFragment() internally
            @Suppress("DEPRECATION")
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, dialogTag)
        } != null
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

    private fun onSelectedModuleChanged(quoteModuleData: QuoteModuleData) {
        this.quoteModuleData = quoteModuleData
        // Update config activity preference
        val configActivity = quoteModuleData.configActivity
        val configActivityPref = findPreference<Preference>(PREF_COMMON_MODULE_PREFERENCES)
        if (configActivity == null) {
            configActivityPref?.isEnabled = false
            configActivityPref?.summary = getString(R.string.pref_module_preferences_summary_alt)
        } else {
            configActivityPref?.isEnabled = true
            configActivityPref?.summary = getString(R.string.pref_module_preferences_summary)
        }

        // Set refresh interval override and disable preference if necessary.
        // This is kind of a lazy solution, but it's better than nothing.
        val minRefreshInterval = quoteModuleData.minimumRefreshInterval
        val refreshIntervalPref = findPreference<Preference>(PREF_COMMON_REFRESH_RATE)
        if (minRefreshInterval != 0) {
            refreshIntervalPref?.isEnabled = false
            refreshIntervalPref?.summary = getString(R.string.pref_refresh_interval_summary_alt)
        } else {
            refreshIntervalPref?.isEnabled = true
            refreshIntervalPref?.summary = getString(R.string.pref_refresh_interval_summary)
        }

        // If the module doesn't require internet connectivity, disable the
        // unmetered only toggle and set the requires internet preference to false.
        val requiresInternet = quoteModuleData.requiresInternetConnectivity
        val unmeteredOnlyPref = findPreference<Preference>(PREF_COMMON_UNMETERED_ONLY)
        if (!requiresInternet) {
            unmeteredOnlyPref?.isEnabled = false
            unmeteredOnlyPref?.summary = getString(R.string.pref_unmetered_only_summary_alt)
        } else {
            unmeteredOnlyPref?.isEnabled = true
            unmeteredOnlyPref?.summary = getString(R.string.pref_unmetered_only_summary)
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
    }
}