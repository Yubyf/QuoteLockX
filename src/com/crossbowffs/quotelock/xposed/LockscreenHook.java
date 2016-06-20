package com.crossbowffs.quotelock.xposed;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.preferences.PrefKeys;
import com.crossbowffs.quotelock.provider.QuoteProvider;
import com.crossbowffs.quotelock.utils.Xlog;
import com.crossbowffs.remotepreferences.RemotePreferences;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.xmlpull.v1.XmlPullParser;

public class LockscreenHook implements IXposedHookZygoteInit, IXposedHookInitPackageResources,
                                       IXposedHookLoadPackage, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = LockscreenHook.class.getSimpleName();

    private static String sModulePath;
    private static XModuleResources sModuleResources;
    private TextView mQuoteTextView;
    private TextView mSourceTextView;

    private static String tryGetString(int resId) {
        try {
            return sModuleResources.getString(resId);
        } catch (Resources.NotFoundException e) {
            Xlog.e(TAG, "Could not find resource: %x", resId);
            return null;
        }
    }

    private static String getQuoteText(SharedPreferences preferences) {
        String text = preferences.getString(PrefKeys.PREF_QUOTES_TEXT, null);
        if (text == null) {
            text = tryGetString(R.string.open_quotelock_app_line1);
        }
        return text;
    }

    private static String getQuoteSource(SharedPreferences preferences) {
        String source = preferences.getString(PrefKeys.PREF_QUOTES_SOURCE, null);
        if (source == null) {
            source = tryGetString(R.string.open_quotelock_app_line2);
        }
        return source;
    }

    private void refreshQuote(SharedPreferences preferences) {
        Xlog.d(TAG, "Quote changed, updating lockscreen layout");
        mQuoteTextView.setText(getQuoteText(preferences));
        mSourceTextView.setText(getQuoteSource(preferences));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        refreshQuote(sharedPreferences);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.android.systemui".equals(lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
            "com.android.keyguard.KeyguardStatusView", lpparam.classLoader,
            "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Xlog.i(TAG, "KeyguardStatusView#onFinishInflate() called, injecting views...");

                    GridLayout self = (GridLayout)param.thisObject;
                    LayoutInflater layoutInflater = LayoutInflater.from(self.getContext());
                    XmlPullParser parser = sModuleResources.getLayout(R.layout.quote_layout);
                    View view = layoutInflater.inflate(parser, null);
                    self.addView(view);

                    mQuoteTextView = (TextView)view.findViewById(R.id.quote_textview);
                    mSourceTextView = (TextView)view.findViewById(R.id.source_textview);

                    RemotePreferences prefs = new RemotePreferences(self.getContext(), QuoteProvider.AUTHORITY, PrefKeys.PREF_QUOTES);
                    refreshQuote(prefs);
                    prefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);

                    Xlog.i(TAG, "View injection complete!");
                }
            });

        Xlog.i(TAG, "QuoteLock Xposed module initialized!");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        sModuleResources = XModuleResources.createInstance(sModulePath, resparam.res);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sModulePath = startupParam.modulePath;
    }
}
