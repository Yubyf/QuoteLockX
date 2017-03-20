package com.crossbowffs.quotelock.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.modules.ModuleManager;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.modules.ModuleNotFoundException;
import com.crossbowffs.quotelock.utils.JobUtils;
import com.crossbowffs.quotelock.consts.Urls;
import com.crossbowffs.quotelock.utils.Xlog;

import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private int mVersionTapCount = 0;
    private ComponentName mModuleConfigActivity = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PrefKeys.PREF_COMMON);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Update version info
        String version = String.format("%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        findPreference(PrefKeys.PREF_ABOUT_VERSION).setSummary(version);

        // Get quote module list
        List<QuoteModule> quoteModules = ModuleManager.getAllModules(getActivity());
        String[] moduleNames = new String[quoteModules.size()];
        String[] moduleClsNames = new String[quoteModules.size()];
        for (int i = 0; i < moduleNames.length; ++i) {
            QuoteModule module = quoteModules.get(i);
            moduleNames[i] = module.getDisplayName(getActivity());
            moduleClsNames[i] = module.getClass().getName();
        }

        // Update quote module list
        ListPreference quoteModulesPref = (ListPreference)findPreference(PrefKeys.PREF_COMMON_QUOTE_MODULE);
        quoteModulesPref.setEntries(moduleNames);
        quoteModulesPref.setEntryValues(moduleClsNames);

        // Update preferences related to module
        onSelectedModuleChanged();
    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
        case PrefKeys.PREF_COMMON_MODULE_PREFERENCES:
            startActivity(mModuleConfigActivity);
            return true;
        case PrefKeys.PREF_ABOUT_AUTHOR_CROSSBOWFFS:
            startBrowserActivity(Urls.TWITTER_CROSSBOWFFS);
            return true;
        case PrefKeys.PREF_ABOUT_AUTHOR_YUBYF:
            startBrowserActivity(Urls.GITHUB_YUBYF);
            return true;
        case PrefKeys.PREF_ABOUT_GITHUB:
            startBrowserActivity(Urls.GITHUB_QUOTELOCK);
            return true;
        case PrefKeys.PREF_ABOUT_VERSION:
            if (++mVersionTapCount == 7) {
                mVersionTapCount = 0;
                Toast.makeText(getActivity(), R.string.pref_about_easter_egg, Toast.LENGTH_SHORT).show();
            }
            return true;
        default:
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Xlog.i(TAG, "Preference changed: %s", key);
        if (PrefKeys.PREF_COMMON_QUOTE_MODULE.equals(key)) {
            onSelectedModuleChanged();
        } else {
            JobUtils.createQuoteDownloadJob(getActivity(), true);
        }
    }

    private QuoteModule loadSelectedModule(SharedPreferences prefs) {
        String moduleClsName = prefs.getString(PrefKeys.PREF_COMMON_QUOTE_MODULE, PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
        try {
            return ModuleManager.getModule(getActivity(), moduleClsName);
        } catch (ModuleNotFoundException e) {
            // Reset to the default module if the currently
            // selected one was not found. Change through the
            // ListPreference so that it updates its value.
            ListPreference quoteModulesPref = (ListPreference)findPreference(PrefKeys.PREF_COMMON_QUOTE_MODULE);
            quoteModulesPref.setValue(PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
            Toast.makeText(getActivity(), R.string.selected_module_not_found, Toast.LENGTH_SHORT).show();
            return loadSelectedModule(prefs);
        }
    }

    private void onSelectedModuleChanged() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        QuoteModule module = loadSelectedModule(prefs);

        // Update config activity preference
        ComponentName configActivity = module.getConfigActivity(getActivity());
        Preference configActivityPref = findPreference(PrefKeys.PREF_COMMON_MODULE_PREFERENCES);
        if (configActivity == null) {
            configActivityPref.setEnabled(false);
            configActivityPref.setSummary(getString(R.string.pref_module_preferences_summary_alt));
            mModuleConfigActivity = null;
        } else {
            configActivityPref.setEnabled(true);
            configActivityPref.setSummary(getString(R.string.pref_module_preferences_summary));
            mModuleConfigActivity = configActivity;
        }

        // Set refresh interval override and disable preference if necessary.
        // This is kind of a lazy solution, but it's better than nothing.
        int minRefreshInterval = module.getMinimumRefreshInterval(getActivity());
        Preference refreshIntervalPref = findPreference(PrefKeys.PREF_COMMON_REFRESH_RATE);
        if (minRefreshInterval != 0) {
            prefs.edit().putInt(PrefKeys.PREF_COMMON_REFRESH_RATE_OVERRIDE, minRefreshInterval).apply();
            refreshIntervalPref.setEnabled(false);
            refreshIntervalPref.setSummary(getString(R.string.pref_refresh_interval_summary_alt));
        } else {
            prefs.edit().remove(PrefKeys.PREF_COMMON_REFRESH_RATE_OVERRIDE).apply();
            refreshIntervalPref.setEnabled(true);
            refreshIntervalPref.setSummary(getString(R.string.pref_refresh_interval_summary));
        }

        // If the module doesn't require internet connectivity, disable the
        // unmeterd only toggle and set the requires internet preference to false.
        boolean requiresInternet = module.requiresInternetConnectivity(getActivity());
        Preference unmeteredOnlyPref = findPreference(PrefKeys.PREF_COMMON_UNMETERED_ONLY);
        if (!requiresInternet) {
            prefs.edit().putBoolean(PrefKeys.PREF_COMMON_REQUIRES_INTERNET, false).apply();
            unmeteredOnlyPref.setEnabled(false);
            unmeteredOnlyPref.setSummary(getString(R.string.pref_unmetered_only_summary_alt));
        } else {
            prefs.edit().remove(PrefKeys.PREF_COMMON_REQUIRES_INTERNET).apply();
            unmeteredOnlyPref.setEnabled(true);
            unmeteredOnlyPref.setSummary(getString(R.string.pref_unmetered_only_summary));
        }

        // Update internet module initially
        if (module.requiresInternetConnectivity(getActivity())) {
            new QuoteDownloaderTask(getActivity()).execute();
        }
    }

    private void startActivity(ComponentName componentName) {
        if (componentName != null) {
            Intent intent = new Intent();
            intent.setComponent(componentName);
            startActivity(intent);
        }
    }

    private void startBrowserActivity(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
