package com.crossbowffs.quotelock.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.provider.PreferenceProvider;
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
    private static final String RES_ID_QUOTE_TEXTVIEW = "quote_textview";
    private static final String RES_ID_SOURCE_TEXTVIEW = "source_textview";

    private static String sModulePath;
    private static XSafeModuleResources sModuleRes;
    private TextView mQuoteTextView;
    private TextView mSourceTextView;
    private RemotePreferences mCommonPrefs;
    private RemotePreferences mQuotePrefs;

    private void refreshQuote() {
        Xlog.d(TAG, "Quote changed, updating lockscreen layout");

        // Update quote text
        String text = mQuotePrefs.getString(PrefKeys.PREF_QUOTES_TEXT, null);
        String source = mQuotePrefs.getString(PrefKeys.PREF_QUOTES_SOURCE, null);
        if (text == null || source == null) {
            try {
                text = sModuleRes.getString(RES_STRING_OPEN_APP_1);
                source = sModuleRes.getString(RES_STRING_OPEN_APP_2);
            } catch (Resources.NotFoundException e) {
                Xlog.e(TAG, "Could not load string resource", e);
                text = null;
                source = null;
            }
        }
        mQuoteTextView.setText(text);
        mSourceTextView.setText(source);

        // Hide source textview if there is no source
        if (TextUtils.isEmpty(source)) {
            mSourceTextView.setVisibility(View.GONE);
        } else {
            mSourceTextView.setVisibility(View.VISIBLE);
        }

        // Update font size
        int textFontSize = Integer.parseInt(mCommonPrefs.getString(
            PrefKeys.PREF_COMMON_FONT_SIZE_TEXT, PrefKeys.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT));
        int sourceFontSize = Integer.parseInt(mCommonPrefs.getString(
            PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE, PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT));
        mQuoteTextView.setTextSize(textFontSize);
        mSourceTextView.setTextSize(sourceFontSize);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        refreshQuote();
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
                    if (self.getChildCount() != 1) {
                        return;
                    }
                    LinearLayout linearLayout = (LinearLayout) self.getChildAt(0);
                    Context context = linearLayout.getContext();
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    XmlPullParser parser;
                    try {
                        parser = sModuleRes.getLayout(RES_LAYOUT_QUOTE_LAYOUT);
                    } catch (Resources.NotFoundException e) {
                        Xlog.e(TAG, "Could not find quote layout, aborting", e);
                        return;
                    }
                    View view = layoutInflater.inflate(parser, null);
                    linearLayout.addView(view);

                    try {
                        mQuoteTextView = (TextView)sModuleRes.findViewById(view, RES_ID_QUOTE_TEXTVIEW);
                        mSourceTextView = (TextView)sModuleRes.findViewById(view, RES_ID_SOURCE_TEXTVIEW);
                    } catch (Resources.NotFoundException e) {
                        Xlog.e(TAG, "Could not find text views, aborting", e);
                        return;
                    }

                    Xlog.i(TAG, "View injection complete, registering preferences...");
                    mCommonPrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_COMMON);
                    mCommonPrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);
                    mQuotePrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_QUOTES);
                    mQuotePrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);

                    Xlog.i(TAG, "Preferences registered, performing initial refresh...");
                    refreshQuote();
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
