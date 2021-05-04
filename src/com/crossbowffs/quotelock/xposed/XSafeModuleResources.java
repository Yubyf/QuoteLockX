package com.crossbowffs.quotelock.xposed;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.crossbowffs.quotelock.BuildConfig;
import org.xmlpull.v1.XmlPullParser;

public class XSafeModuleResources {
    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    private final XModuleResources mModuleRes;

    private XSafeModuleResources(XModuleResources moduleRes) {
        mModuleRes = moduleRes;
    }

    public static XSafeModuleResources createInstance(String path, XResources origRes) {
        XModuleResources moduleRes = XModuleResources.createInstance(path, origRes);
        return new XSafeModuleResources(moduleRes);
    }

    private int getResId(String resName, String resType) {
        int resId = mModuleRes.getIdentifier(resName, resType, PACKAGE_NAME);
        if (resId == 0) {
            throw new Resources.NotFoundException("Could not find " + resType + ": " + resName);
        }
        return resId;
    }

    public String getString(String resName) {
        int resId = getResId(resName, "string");
        return mModuleRes.getString(resId);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public Drawable getDrawable(String resName) {
        int resId = getResId(resName, "drawable");
        return mModuleRes.getDrawable(resId, null);
    }

    public XmlPullParser getLayout(String resName) {
        int resId = getResId(resName, "layout");
        return mModuleRes.getLayout(resId);
    }

    public View findViewById(View view, String idName) {
        int id = getResId(idName, "id");
        return view.findViewById(id);
    }
}
