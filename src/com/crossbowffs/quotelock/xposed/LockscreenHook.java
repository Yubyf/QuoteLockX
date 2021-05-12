package com.crossbowffs.quotelock.xposed;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract;
import com.crossbowffs.quotelock.consts.PrefKeys;
import com.crossbowffs.quotelock.provider.ActionProvider;
import com.crossbowffs.quotelock.provider.PreferenceProvider;
import com.crossbowffs.quotelock.utils.DpUtils;
import com.crossbowffs.quotelock.utils.Md5Utils;
import com.crossbowffs.quotelock.utils.Xlog;
import com.crossbowffs.remotepreferences.RemotePreferences;

import org.xmlpull.v1.XmlPullParser;

import java.util.Objects;
import java.util.Set;

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

    private static final String PACKAGE_SYSTEM_UI = "com.android.systemui";

    private static final String RES_LAYOUT_QUOTE_LAYOUT = "quote_layout";
    private static final String RES_STRING_OPEN_APP_1 = "open_quotelock_app_line1";
    private static final String RES_STRING_OPEN_APP_2 = "open_quotelock_app_line2";
    private static final String RES_ID_QUOTE_CONTAINER = "quote_container";
    private static final String RES_ID_QUOTE_TEXTVIEW = "quote_textview";
    private static final String RES_ID_SOURCE_TEXTVIEW = "source_textview";
    private static final String RES_ID_ACTION_CONTAINER = "action_container";
    private static final String RES_ID_REFRESH_IMAGE_VIEW = "refresh_image_view";
    private static final String RES_ID_COLLECT_IMAGE_VIEW = "collect_image_view";

    /** For OnePlus AOD */
    private static final String RES_ID_OP_AOD_CONTAINER = "op_aod_system_info_container";

    private static final String RES_ID_REFRESH_ICON = "ic_baseline_refresh_24dp";
    private static final String RES_ID_COLLECT_ICON = "selector_star";
    private float mLayoutTranslation = -(16F + 32F + 16F + 32F + 16F);
    private static final long LAYOUT_ANIMATION_DURATION = 500;

    private static String sModulePath;
    private static XSafeModuleResources sModuleRes;
    private LinearLayout mQuoteContainer;
    private TextView mQuoteTextView;
    private TextView mSourceTextView;
    private LinearLayout mActionContainer;
    private ImageView mRefreshImageView;
    private ImageView mCollectImageView;

    private LinearLayout mAodQuoteContainer;
    private TextView mAodQuoteTextView;
    private TextView mAodSourceTextView;

    private RemotePreferences mCommonPrefs;
    private RemotePreferences mQuotePrefs;

    private boolean mDisplayOnAod = false;
    private Handler mAodHandler;

    private void refreshLockscreenQuote() {
        Xlog.d(TAG, "Quote changed, updating lockscreen layout");

        // Update quote text
        String text = mQuotePrefs.getString(PrefKeys.PREF_QUOTES_TEXT, null);
        String source = mQuotePrefs.getString(PrefKeys.PREF_QUOTES_SOURCE, null);
        boolean collectionState = mQuotePrefs.getBoolean(PrefKeys.PREF_QUOTES_COLLECTION_STATE, false);
        if (text == null || source == null) {
            try {
                text = sModuleRes.getString(RES_STRING_OPEN_APP_1);
                source = sModuleRes.getString(RES_STRING_OPEN_APP_2);
            } catch (Resources.NotFoundException e) {
                Xlog.e(TAG, "Could not load string resource", e);
                text = null;
                source = null;
            }
            mCollectImageView.setVisibility(View.GONE);
            mLayoutTranslation = -(16F + 32F + 16F + 1F);
        } else {
            mCollectImageView.setVisibility(View.VISIBLE);
            mLayoutTranslation = -(16F + 32F + 16F + 32F + 16F + 1F);
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mActionContainer.getLayoutParams();
        params.rightMargin = (int) DpUtils.dp2px(mActionContainer.getContext(), mLayoutTranslation + 16F);
        mActionContainer.setLayoutParams(params);
        mQuoteTextView.setText(text);
        mSourceTextView.setText(source);
        mCollectImageView.setSelected(collectionState);

        // Hide source textview if there is no source
        if (TextUtils.isEmpty(source)) {
            mSourceTextView.setVisibility(View.GONE);
        } else {
            mSourceTextView.setVisibility(View.VISIBLE);
        }

        // Update layout padding
        int paddingTop = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_PADDING_TOP, PrefKeys.PREF_COMMON_PADDING_TOP_DEFAULT));
        int paddingBottom = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_PADDING_BOTTOM, PrefKeys.PREF_COMMON_PADDING_BOTTOM_DEFAULT));
        mQuoteContainer.setPadding(mQuoteContainer.getPaddingStart(),
                (int) DpUtils.dp2px(mQuoteContainer.getContext(), paddingTop),
                mQuoteContainer.getPaddingEnd(),
                (int) DpUtils.dp2px(mQuoteContainer.getContext(), paddingBottom));

        // Update font size
        int textFontSize = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_FONT_SIZE_TEXT, PrefKeys.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT));
        int sourceFontSize = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE, PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT));
        mQuoteTextView.setTextSize(textFontSize);
        mSourceTextView.setTextSize(sourceFontSize);

        // Font properties
        Set<String> quoteStyles = mCommonPrefs.getStringSet(PrefKeys.PREF_COMMON_FONT_STYLE_TEXT, null);
        Set<String> sourceStyles = mCommonPrefs.getStringSet(PrefKeys.PREF_COMMON_FONT_STYLE_SOURCE, null);
        int quoteStyle = getTypefaceStyle(quoteStyles);
        int sourceStyle = getTypefaceStyle(sourceStyles);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String font = mCommonPrefs.getString(
                    PrefKeys.PREF_COMMON_FONT_FAMILY, PrefKeys.PREF_COMMON_FONT_FAMILY_DEFAULT);
            if (!PrefKeys.PREF_COMMON_FONT_FAMILY_DEFAULT.equals(font)) {
                mQuoteTextView.setTypeface(sModuleRes.getFont(font), quoteStyle);
                mSourceTextView.setTypeface(sModuleRes.getFont(font), sourceStyle);
            } else {
                mQuoteTextView.setTypeface(null, quoteStyle);
                mSourceTextView.setTypeface(null, sourceStyle);
            }
        } else {
            mQuoteTextView.setTypeface(null, quoteStyle);
            mSourceTextView.setTypeface(null, sourceStyle);
        }
    }

    private void refreshAodQuote() {
        Xlog.d(TAG, "Quote changed, updating aod layout. " +
                "Current thread [" + Thread.currentThread().getId() + "]" +
                ((Looper.myLooper() == Looper.getMainLooper()) ? " is UI-Thread" : ""));

        mDisplayOnAod = mCommonPrefs.getBoolean(PrefKeys.PREF_COMMON_DISPLAY_ON_AOD, false);

        if (!mDisplayOnAod) {
            if (mAodQuoteContainer != null) {
                mAodQuoteContainer.setVisibility(View.GONE);
            }
            return;
        } else {
            if (mAodQuoteContainer != null) {
                mAodQuoteContainer.setVisibility(View.VISIBLE);
            }
        }
        if (!isAodViewAvailable()) {
            return;
        }

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
        mAodQuoteTextView.setText(text);
        mAodSourceTextView.setText(source);

        // Hide source textview if there is no source
        if (TextUtils.isEmpty(source)) {
            mAodSourceTextView.setVisibility(View.GONE);
        } else {
            mAodSourceTextView.setVisibility(View.VISIBLE);
        }

        // Update layout padding
        int paddingTop = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_PADDING_TOP, PrefKeys.PREF_COMMON_PADDING_TOP_DEFAULT));
        int paddingBottom = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_PADDING_BOTTOM, PrefKeys.PREF_COMMON_PADDING_BOTTOM_DEFAULT));
        mAodQuoteContainer.setPadding(mAodQuoteContainer.getPaddingStart(),
                (int) DpUtils.dp2px(mAodQuoteContainer.getContext(), paddingTop),
                mAodQuoteContainer.getPaddingEnd(),
                (int) DpUtils.dp2px(mAodQuoteContainer.getContext(), paddingBottom));

        // Update font size
        int textFontSize = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_FONT_SIZE_TEXT, PrefKeys.PREF_COMMON_FONT_SIZE_TEXT_DEFAULT));
        int sourceFontSize = Integer.parseInt(mCommonPrefs.getString(
                PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE, PrefKeys.PREF_COMMON_FONT_SIZE_SOURCE_DEFAULT));
        mAodQuoteTextView.setTextSize(textFontSize);
        mAodSourceTextView.setTextSize(sourceFontSize);

        // Font properties
        Set<String> quoteStyles = mCommonPrefs.getStringSet(PrefKeys.PREF_COMMON_FONT_STYLE_TEXT, null);
        Set<String> sourceStyles = mCommonPrefs.getStringSet(PrefKeys.PREF_COMMON_FONT_STYLE_SOURCE, null);
        int quoteStyle = getTypefaceStyle(quoteStyles);
        int sourceStyle = getTypefaceStyle(sourceStyles);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String font = mCommonPrefs.getString(
                    PrefKeys.PREF_COMMON_FONT_FAMILY, PrefKeys.PREF_COMMON_FONT_FAMILY_DEFAULT);
            if (!PrefKeys.PREF_COMMON_FONT_FAMILY_DEFAULT.equals(font)) {
                mAodQuoteTextView.setTypeface(sModuleRes.getFont(font), quoteStyle);
                mAodSourceTextView.setTypeface(sModuleRes.getFont(font), sourceStyle);
            } else {
                mAodQuoteTextView.setTypeface(null, quoteStyle);
                mAodSourceTextView.setTypeface(null, sourceStyle);
            }
        } else {
            mAodQuoteTextView.setTypeface(null, quoteStyle);
            mAodSourceTextView.setTypeface(null, sourceStyle);
        }
    }

    private boolean isAodViewAvailable() {
        return mDisplayOnAod && mAodQuoteContainer != null && mAodQuoteTextView != null && mAodSourceTextView != null;
    }

    private int getTypefaceStyle(Set<String> styles) {
        int style = Typeface.NORMAL;
        if (styles != null) {
            if (styles.contains("bold") && styles.contains("italic")) {
                style = Typeface.BOLD_ITALIC;
            } else if (styles.contains("bold")) {
                style = Typeface.BOLD;
            } else if (styles.contains("italic")) {
                style = Typeface.ITALIC;
            }
        }
        return style;
    }

    private void refreshQuoteRemote(Context context) {
        Uri uri = Uri.parse("content://" + ActionProvider.AUTHORITY).buildUpon()
                .appendPath("refresh").build();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.close();
        }
    }

    private void collectQuoteRemote(Context context) {
        String text = mQuoteTextView.getText().toString();
        String source = mSourceTextView.getText().toString().replace("―", "").trim();

        Uri uri = ActionProvider.CONTENT_URI.buildUpon().appendPath("collect").build();
        ContentValues values = new ContentValues(3);
        values.put(QuoteCollectionContract.Collections.TEXT, text);
        values.put(QuoteCollectionContract.Collections.SOURCE, source);
        values.put(QuoteCollectionContract.Collections.MD5, Md5Utils.md5(text + source));
        ContentResolver resolver = context.getContentResolver();
        Uri resultUri = resolver.insert(uri, values);
        if (Objects.equals(resultUri.getLastPathSegment(), "-1")) {
            resetTranslationAnimator();
        }
    }

    private void deleteCollectedQuoteRemote(Context context) {
        String text = mQuoteTextView.getText().toString();
        String source = mSourceTextView.getText().toString().replace("―", "").trim();

        Uri uri = ActionProvider.CONTENT_URI.buildUpon().appendPath("collect").build();
        ContentResolver resolver = context.getContentResolver();
        int result = resolver.delete(uri,
                QuoteCollectionContract.Collections.MD5 + "='" + Md5Utils.md5(text + source) + "'",
                null);
        if (result < 0) {
            resetTranslationAnimator();
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
                .translationX(DpUtils.dp2px(context, mLayoutTranslation))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(LAYOUT_ANIMATION_DURATION)
                .start();
        mActionContainer.animate()
                .translationX(DpUtils.dp2px(context, mLayoutTranslation))
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
        refreshLockscreenQuote();
        if (mAodHandler != null) {
            // Refresh the injected AOD quote views on thread of AOD layout.
            mAodHandler.post(this::refreshAodQuote);
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!PACKAGE_SYSTEM_UI.equals(lpparam.packageName)) {
            return;
        }

        hookLockscreenLayout(lpparam);
        hookLockscreenClick(lpparam);
        hookAodLayout(lpparam);
        Xlog.i(TAG, "QuoteLock Xposed module initialized!");
    }

    private void hookLockscreenLayout(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                "com.android.keyguard.KeyguardStatusView", lpparam.classLoader,
                "onFinishInflate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Xlog.i(TAG, "KeyguardStatusView#onFinishInflate() called, injecting views...");
                        GridLayout self = (GridLayout) param.thisObject;
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
                            mQuoteContainer = (LinearLayout) sModuleRes.findViewById(view, RES_ID_QUOTE_CONTAINER);
                            mQuoteTextView = (TextView) sModuleRes.findViewById(view, RES_ID_QUOTE_TEXTVIEW);
                            mSourceTextView = (TextView) sModuleRes.findViewById(view, RES_ID_SOURCE_TEXTVIEW);
                            mActionContainer = (LinearLayout) sModuleRes.findViewById(view, RES_ID_ACTION_CONTAINER);
                            mRefreshImageView = (ImageView) sModuleRes.findViewById(view, RES_ID_REFRESH_IMAGE_VIEW);
                            Drawable refreshIcon = sModuleRes.getDrawable(RES_ID_REFRESH_ICON);
                            if (refreshIcon != null) {
                                mRefreshImageView.setImageDrawable(refreshIcon);
                            }
                            mCollectImageView = (ImageView) sModuleRes.findViewById(view, RES_ID_COLLECT_IMAGE_VIEW);
                            Drawable collectIcon = sModuleRes.getDrawable(RES_ID_COLLECT_ICON);
                            if (collectIcon != null) {
                                mCollectImageView.setImageDrawable(collectIcon);
                            }
                        } catch (Resources.NotFoundException e) {
                            Xlog.e(TAG, "Could not find text views, aborting", e);
                            return;
                        }

                        mQuoteContainer.setOnLongClickListener(v -> {
                            Xlog.d(TAG, "QuoteContainer onLongClick");
                            if (mQuoteContainer.getTranslationX() != 0) {
                                resetTranslationAnimator();
                            } else {
                                setTranslationAnimator();
                            }
                            return true;
                        });

                        mRefreshImageView.setOnClickListener(v -> {
                            setRefreshAnimator();
                            refreshQuoteRemote(v.getContext());
                        });

                        mCollectImageView.setOnClickListener(v -> {
                            if (v.isSelected()) {
                                deleteCollectedQuoteRemote(v.getContext());
                            } else {
                                collectQuoteRemote(v.getContext());
                            }
                        });

                        Xlog.i(TAG, "View injection complete, registering preferences...");
                        mCommonPrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_COMMON);
                        mCommonPrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);
                        mQuotePrefs = new RemotePreferences(context, PreferenceProvider.AUTHORITY, PrefKeys.PREF_QUOTES);
                        mQuotePrefs.registerOnSharedPreferenceChangeListener(LockscreenHook.this);

                        Xlog.i(TAG, "Preferences registered, performing initial refresh...");
                        refreshLockscreenQuote();
                    }
                });
    }

    private void hookLockscreenClick(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.R ?
                        "com.android.systemui.statusbar.phone.PanelView" :
                        "com.android.systemui.statusbar.phone.PanelViewController",
                lpparam.classLoader,
                "onEmptySpaceClick", float.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Xlog.i(TAG, "PanelViewController#onEmptySpaceClick() called, reset QuoteContainer position...");
                        if (mQuoteContainer == null || !mQuoteContainer.isAttachedToWindow()) {
                            Xlog.e(TAG, "QuoteContainer is empty or not attached to window");
                            return;
                        }
                        resetTranslationAnimator();
                    }
                });
    }

    private void hookAodLayout(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                "com.oneplus.aod.OpClockViewCtrl", lpparam.classLoader,
                "initViews", ViewGroup.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Xlog.i(TAG, "com.oneplus.aop.OpClockViewCtrl#initViews() called, injecting views...");
                        // This method is not called on UI thread, so the injected views should be refreshed on current thread.
                        mAodHandler = new Handler(Looper.myLooper());
                        ViewGroup root = (ViewGroup) param.args[0];
                        Xlog.d(TAG, "OpClockViewCtrl root " + root);
                        final Context context = root.getContext();
                        LinearLayout opAodContainer = (LinearLayout) root.findViewById(
                                context.getResources().getIdentifier(RES_ID_OP_AOD_CONTAINER,
                                        "id", PACKAGE_SYSTEM_UI));
                        Xlog.d(TAG, "OpClockViewCtrl opAodContainer" + opAodContainer);
                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        XmlPullParser parser;
                        try {
                            parser = sModuleRes.getLayout(RES_LAYOUT_QUOTE_LAYOUT);
                        } catch (Resources.NotFoundException e) {
                            Xlog.e(TAG, "Could not find quote layout, aborting", e);
                            return;
                        }
                        View view = layoutInflater.inflate(parser, null);
                        opAodContainer.addView(view);

                        try {
                            mAodQuoteContainer = (LinearLayout) sModuleRes.findViewById(view, RES_ID_QUOTE_CONTAINER);
                            mAodQuoteTextView = (TextView) sModuleRes.findViewById(view, RES_ID_QUOTE_TEXTVIEW);
                            mAodSourceTextView = (TextView) sModuleRes.findViewById(view, RES_ID_SOURCE_TEXTVIEW);
                            LinearLayout aodActionContainer = (LinearLayout) sModuleRes.findViewById(view, RES_ID_ACTION_CONTAINER);
                            aodActionContainer.setVisibility(View.GONE);
                        } catch (Resources.NotFoundException e) {
                            Xlog.e(TAG, "Could not find text views, aborting", e);
                        }
                    }
                });
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
