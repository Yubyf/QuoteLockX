package com.crossbowffs.quotelock.app;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteModule;
import com.crossbowffs.quotelock.collections.app.QuoteCollectionActivity;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.consts.Urls;
import com.crossbowffs.quotelock.modules.ModuleManager;
import com.crossbowffs.quotelock.modules.ModuleNotFoundException;
import com.crossbowffs.quotelock.utils.JobUtils;
import com.crossbowffs.quotelock.utils.Xlog;
import com.crossbowffs.quotelock.utils.XposedUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private int mVersionTapCount = 0;
    private ComponentName mModuleConfigActivity = null;

    private SharedPreferences mQuotesPreferences;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PrefKeys.PREF_COMMON);
        setPreferencesFromResource(R.xml.settings, rootKey);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        mQuotesPreferences = requireContext().getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE);
        mQuotesPreferences.registerOnSharedPreferenceChangeListener(this);

        // Only enable DisplayOnAOD on tested devices.
        if (!XposedUtils.isAodHookAvailable()) {
            findPreference(PrefKeys.PREF_COMMON_DISPLAY_ON_AOD).setVisible(false);
        }

        // Only enable font family above API26
        findPreference(PrefKeys.PREF_COMMON_FONT_FAMILY).setEnabled(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);

        // Update version info
        String version = String.format(Locale.getDefault(),
                "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        findPreference(PrefKeys.PREF_ABOUT_VERSION).setSummary(version);

        // Last update info
        long lastUpdate = mQuotesPreferences.getLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, -1);
        findPreference(PrefKeys.PREF_COMMON_UPDATE_INFO).setSummary(
                getString(R.string.pref_refresh_info_summary, lastUpdate > 0 ?
                        DATE_FORMATTER.format(new Date(lastUpdate)) : "-"));

        // Get quote module list
        List<QuoteModule> quoteModules = ModuleManager.getAllModules(requireContext());
        String[] moduleNames = new String[quoteModules.size()];
        String[] moduleClsNames = new String[quoteModules.size()];
        for (int i = 0; i < moduleNames.length; ++i) {
            QuoteModule module = quoteModules.get(i);
            moduleNames[i] = module.getDisplayName(requireContext());
            moduleClsNames[i] = module.getClass().getName();
        }

        // Update quote module list
        ListPreference quoteModulesPref = findPreference(PrefKeys.PREF_COMMON_QUOTE_MODULE);
        quoteModulesPref.setEntries(moduleNames);
        quoteModulesPref.setEntryValues(moduleClsNames);

        // Update preferences related to module
        onSelectedModuleChanged();
    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mQuotesPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case PrefKeys.PREF_COMMON_MODULE_PREFERENCES:
                startActivity(mModuleConfigActivity);
                return true;
            case PrefKeys.PREF_FEATURES_COLLECTION:
                startActivity(new ComponentName(requireContext(), QuoteCollectionActivity.class));
                return true;
            case PrefKeys.PREF_ABOUT_CREDITS:
                showCreditsDialog();
                return true;
            case PrefKeys.PREF_ABOUT_GITHUB:
                startBrowserActivity(Urls.GITHUB_QUOTELOCK);
                return true;
            case PrefKeys.PREF_ABOUT_GITHUB_CURRENT:
                startBrowserActivity(Urls.GITHUB_QUOTELOCK_CURRENT);
                return true;
            case PrefKeys.PREF_ABOUT_VERSION:
                if (++mVersionTapCount == 7) {
                    mVersionTapCount = 0;
                    Toast.makeText(requireContext(), R.string.easter_egg, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Xlog.i(TAG, "Preference changed: %s", key);
        if (Objects.equals(sharedPreferences, mQuotesPreferences)) {
            if (PrefKeys.PREF_QUOTES_LAST_UPDATED.equals(key)) {
                long lastUpdate = mQuotesPreferences.getLong(PrefKeys.PREF_QUOTES_LAST_UPDATED, -1);
                findPreference(PrefKeys.PREF_COMMON_UPDATE_INFO).setSummary(
                        getString(R.string.pref_refresh_info_summary, lastUpdate > 0 ?
                                DATE_FORMATTER.format(new Date(lastUpdate)) : "-"));
            }
            return;
        }
        if (PrefKeys.PREF_COMMON_QUOTE_MODULE.equals(key)) {
            onSelectedModuleChanged();
        } else {
            JobUtils.createQuoteDownloadJob(requireContext(), true);
        }
    }

    private QuoteModule loadSelectedModule(SharedPreferences prefs) {
        String moduleClsName = prefs.getString(PrefKeys.PREF_COMMON_QUOTE_MODULE, PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
        try {
            return ModuleManager.getModule(requireContext(), moduleClsName);
        } catch (ModuleNotFoundException e) {
            // Reset to the default module if the currently
            // selected one was not found. Change through the
            // ListPreference so that it updates its value.
            ListPreference quoteModulesPref = findPreference(PrefKeys.PREF_COMMON_QUOTE_MODULE);
            quoteModulesPref.setValue(PrefKeys.PREF_COMMON_QUOTE_MODULE_DEFAULT);
            Toast.makeText(requireContext(), R.string.selected_module_not_found, Toast.LENGTH_SHORT).show();
            return loadSelectedModule(prefs);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (PrefKeys.PREF_COMMON_FONT_FAMILY.equals(preference.getKey())) {
            showFontFamilyDialog(preference);
            return;
        }
        super.onDisplayPreferenceDialog(preference);
    }

    private void showFontFamilyDialog(Preference preference) {
        // check if dialog is already showing
        String dialogFragmentTag = null;
        try {
            Class<?> clazz = getClass().getSuperclass();
            Field field = clazz.getDeclaredField("DIALOG_FRAGMENT_TAG");
            field.setAccessible(true);
            Object tag = field.get(this);
            if (tag instanceof String) {
                dialogFragmentTag = (String) tag;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG";
            e.printStackTrace();
        }
        if (requireFragmentManager().findFragmentByTag(dialogFragmentTag) != null) {
            return;
        }
        final DialogFragment f;
        f = FontListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        f.setTargetFragment(this, 0);
        f.show(requireFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
    }

    private void showCreditsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.credits_title)
                .setMessage(Html.fromHtml(getString(R.string.credits_message)))
                .setPositiveButton(R.string.close, null)
                .show();
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void onSelectedModuleChanged() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        QuoteModule module = loadSelectedModule(prefs);

        // Update config activity preference
        ComponentName configActivity = module.getConfigActivity(requireContext());
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
        int minRefreshInterval = module.getMinimumRefreshInterval(requireContext());
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
        // unmetered only toggle and set the requires internet preference to false.
        boolean requiresInternet = module.requiresInternetConnectivity(requireContext());
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
        if (module.requiresInternetConnectivity(requireContext())) {
            new QuoteDownloaderTask(requireContext()).execute();
        }

        // Update font family options
        ListPreference fontFamilyPref = findPreference(PrefKeys.PREF_COMMON_FONT_FAMILY);
        int entries = R.array.font_family_entries;
        int values = R.array.font_family_values;
        if (QuoteModule.CHARACTER_TYPE_LATIN == module.getCharacterType()) {
            entries = R.array.font_family_latin_entries;
            values = R.array.font_family_latin_values;
        } else if (QuoteModule.CHARACTER_TYPE_CJK == module.getCharacterType()) {
            entries = R.array.font_family_cjk_entries;
            values = R.array.font_family_cjk_values;
        }
        fontFamilyPref.setEntries(entries);
        fontFamilyPref.setEntryValues(values);
        if (fontFamilyPref.findIndexOfValue(fontFamilyPref.getValue()) < 0) {
            fontFamilyPref.setValueIndex(0);
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
