package com.crossbowffs.quotelock.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.provider.ActionProvider;
import com.crossbowffs.quotelock.provider.PreferenceProvider;
import com.crossbowffs.quotelock.utils.DpUtils;
import com.crossbowffs.quotelock.utils.Xlog;
import com.crossbowffs.remotepreferences.RemotePreferences;

import org.xmlpull.v1.XmlPullParser;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LockscreenHook implements IXposedHookZygoteInit, IXposedHookInitPackageResources,
                                       IXposedHookLoadPackage, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = LockscreenHook.class.getSimpleName();
    private static final String RES_LAYOUT_QUOTE_LAYOUT = "quote_layout";
    private static final String RES_STRING_OPEN_APP_1 = "open_quotelock_app_line1";
    private static final String RES_STRING_OPEN_APP_2 = "open_quotelock_app_line2";
    private static final String RES_ID_QUOTE_CONTAINER = "quote_container";
    private static final String RES_ID_QUOTE_TEXTVIEW = "quote_textview";
    private static final String RES_ID_SOURCE_TEXTVIEW = "source_textview";
    private static final String RES_ID_ACTION_CONTAINER = "action_container";
    private static final String RES_ID_REFRESH_IMAGE_VIEW = "refresh_image_view";

    private static final String RES_ID_REFRESH_ICON = "ic_refresh_white_24dp";
    private static final float LAYOUT_TRANSLATION = -(16F + 32F + 16F);
    private static final long LAYOUT_ANIMATION_DURATION = 500;

    private static String sModulePath;
    private static XSafeModuleResources sModuleRes;
    private LinearLayout mQuoteContainer;
    private TextView mQuoteTextView;
    private TextView mSourceTextView;
    private LinearLayout mActionContainer;
    private ImageView mRefreshImageView;
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

    private void refreshQuoteRemote(Context context) {
        Uri uri = Uri.parse("content://" + ActionProvider.AUTHORITY).buildUpon()
                .appendPath("refresh").build();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.close();
        }
    }

    private void setRefreshAnimator() {
        mRefreshImageView.setRotation(0);
        mRefreshImageView.animate()
                .rotationBy(360)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshImageView.animate()
                                .rotationBy(360)
                                .setInterpolator(new LinearInterpolator())
                                .withEndAction(this)
                                .setDuration(LAYOUT_ANIMATION_DURATION)
                                .start();
                    }
                })
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
    }

    private void resetRefreshAnimator() {
        mRefreshImageView.animate().cancel();
        if (mRefreshImageView.getRotation() > 0) {
            float remainDegree = (360 - mRefreshImageView.getRotation() % 360);
            mRefreshImageView.animate()
                    .setDuration((long) (LAYOUT_ANIMATION_DURATION / 360F * remainDegree))
                    .setInterpolator(new DecelerateInterpolator())
                    .rotationBy(remainDegree)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshImageView.setRotation(0);
                        }
                    })
                    .start();
        }
    }

    private void setTranslationAnimator() {
        Context context = mQuoteContainer.getContext();
        mQuoteContainer.animate()
                .translationX(DpUtils.dp2px(context, LAYOUT_TRANSLATION))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
        mActionContainer.animate()
                .translationX(DpUtils.dp2px(context, LAYOUT_TRANSLATION))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
    }

    private void resetTranslationAnimator() {
        resetRefreshAnimator();
        mQuoteContainer.animate().cancel();
        mActionContainer.animate().cancel();
        mQuoteContainer.animate()
                .translationX(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
        mActionContainer.animate()
                .translationX(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        resetTranslationAnimator();
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
                    final Context context = linearLayout.getContext();
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
                        mQuoteContainer = (LinearLayout)sModuleRes.findViewById(view, RES_ID_QUOTE_CONTAINER);
                        mQuoteTextView = (TextView)sModuleRes.findViewById(view, RES_ID_QUOTE_TEXTVIEW);
                        mSourceTextView = (TextView)sModuleRes.findViewById(view, RES_ID_SOURCE_TEXTVIEW);
                        mActionContainer = (LinearLayout) sModuleRes.findViewById(view, RES_ID_ACTION_CONTAINER);
                        mRefreshImageView = (ImageView)sModuleRes.findViewById(view, RES_ID_REFRESH_IMAGE_VIEW);
                        Drawable drawable = sModuleRes.getDrawable(RES_ID_REFRESH_ICON);
                        if (drawable != null) {
                            mRefreshImageView.setImageDrawable(drawable);
                        }
                    } catch (Resources.NotFoundException e) {
                        Xlog.e(TAG, "Could not find text views, aborting", e);
                        return;
                    }

                    mQuoteContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Xlog.d(TAG, "QuoteContainer onLongClick");
                            if (mQuoteContainer.getTranslationX() != 0) {
                                resetTranslationAnimator();
                            } else {
                                setTranslationAnimator();
                            }
                            return true;
                        }
                    });

                    mRefreshImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setRefreshAnimator();
                            refreshQuoteRemote(v.getContext());
                        }
                    });

                    Xlog.i(TAG, "View injection complete, registering preferences...");
                    mCommonPrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_COMMON);
                    mCommonPrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);
                    mQuotePrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_QUOTES);
                    mQuotePrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);

                    Xlog.i(TAG, "Preferences registered, performing initial refresh...");
                    refreshQuote();
                }
            });
        XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader,
                "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        MotionEvent event = (MotionEvent) param.args[0];
                        if (event == null) {
                            return;
                        }
                        Xlog.i(TAG, "NotificationPanelView#onTouchEvent() called, reset QuoteContainer position...");
                        if (mQuoteContainer == null || !mQuoteContainer.isAttachedToWindow()) {
                            Xlog.e(TAG, "QuoteContainer is empty or not attached to window");
                            return;
                        }
                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            resetTranslationAnimator();
                            Xlog.d(TAG, "QuoteContainer position reset completely");
                        }
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
