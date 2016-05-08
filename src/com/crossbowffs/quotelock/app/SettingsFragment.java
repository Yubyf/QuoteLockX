package com.crossbowffs.quotelock.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.utils.JobUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final String TWITTER_URL = "https://twitter.com/crossbowffs";
    private static final String GITHUB_URL = "https://github.com/apsun/QuoteLock";
    private static final String VNAAS_URL = "http://vnaas.apsun.xyz/";

    private int mVersionTapCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PrefKeys.PREF_COMMON);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.settings);
        String version = String.format("%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        findPreference(PrefKeys.PREF_ABOUT_VERSION).setSummary(version);
    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
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
    }

    private void startBrowserActivity(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
