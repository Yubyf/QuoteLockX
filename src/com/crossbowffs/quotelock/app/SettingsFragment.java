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
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.utils.JobUtils;

import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TWITTER_URL = "https://twitter.com/crossbowffs";
    private static final String GITHUB_URL = "https://github.com/apsun/QuoteLock";
    private static final String VNAAS_URL = "http://vnaas.apsun.xyz/";

    private int mVersionTapCount = 0;
    private ComponentName mModuleConfigActivity = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PrefKeys.PREF_COMMON);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.settings);

        // Update version info
        String version = String.format("%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        findPreference(PrefKeys.PREF_ABOUT_VERSION).setSummary(version);

        // Get quote module list
        List<QuoteModule> quoteModules = ModuleManager.getAllModules();
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

        // Update quote module config link
        updateConfigActivityLink();
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
            Intent intent = new Intent();
            intent.setComponent(mModuleConfigActivity);
            startActivity(intent);
            return true;
        case PrefKeys.PREF_ABOUT_TWITTER:
            startBrowserActivity(TWITTER_URL);
            return true;
        case PrefKeys.PREF_ABOUT_GITHUB:
            startBrowserActivity(GITHUB_URL);
            return true;
        case PrefKeys.PREF_ABOUT_VNAAS:
            startBrowserActivity(VNAAS_URL);
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
        JobUtils.createQuoteDownloadJob(getActivity());
        if (PrefKeys.PREF_COMMON_QUOTE_MODULE.equals(key)) {
            updateConfigActivityLink();
        }
    }

    private void updateConfigActivityLink() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String moduleClsName = prefs.getString(PrefKeys.PREF_COMMON_QUOTE_MODULE, PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
        QuoteModule module = ModuleManager.getModule(moduleClsName);
        ComponentName configActivity = module.getConfigActivity(getActivity());
        Preference configActivityLink = findPreference(PrefKeys.PREF_COMMON_MODULE_PREFERENCES);
        if (configActivity == null) {
            configActivityLink.setEnabled(false);
            mModuleConfigActivity = null;
        } else {
            configActivityLink.setEnabled(true);
            mModuleConfigActivity = configActivity;
        }
    }

    private void startBrowserActivity(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
