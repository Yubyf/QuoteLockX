package com.crossbowffs.quotelock.xposed;

import android.content.SharedPreferences;
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
    private static final String RES_LAYOUT_QUOTE_LAYOUT = "quote_layout";
    private static final String RES_STRING_OPEN_APP_1 = "open_quotelock_app_line1";
    private static final String RES_STRING_OPEN_APP_2 = "open_quotelock_app_line2";

    private static String sModulePath;
    private static XSafeModuleResources sModuleRes;
    private TextView mQuoteTextView;
    private TextView mSourceTextView;

    private void refreshQuote(SharedPreferences preferences) {
        Xlog.d(TAG, "Quote changed, updating lockscreen layout");
        String text = preferences.getString(PrefKeys.PREF_QUOTES_TEXT, null);
        String source = preferences.getString(PrefKeys.PREF_QUOTES_SOURCE, null);
        if (text == null || source == null) {
            text = sModuleRes.getString(RES_STRING_OPEN_APP_1);
            source = sModuleRes.getString(RES_STRING_OPEN_APP_2);
        }
        mQuoteTextView.setText(text);
        mSourceTextView.setText(source);
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
                    XmlPullParser parser = sModuleRes.getLayout(RES_LAYOUT_QUOTE_LAYOUT);
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
        sModuleRes = XSafeModuleResources.createInstance(sModulePath, resparam.res);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sModulePath = startupParam.modulePath;
    }
}
