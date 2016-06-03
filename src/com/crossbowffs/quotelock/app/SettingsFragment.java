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
            startActivity(mModuleConfigActivity);
            return true;
        case PrefKeys.PREF_ABOUT_AUTHOR_CROSSBOWFFS:
            startBrowserActivity("https://twitter.com/crossbowffs");
            return true;
        case PrefKeys.PREF_ABOUT_AUTHOR_YUBYF:
            startBrowserActivity("https://github.com/Yubyf");
            return true;
        case PrefKeys.PREF_ABOUT_GITHUB:
            startBrowserActivity("https://github.com/apsun/QuoteLock");
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
        JobUtils.createQuoteDownloadJob(getActivity(), true);
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
