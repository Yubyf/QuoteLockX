package com.crossbowffs.quotelock.xposed;

import android.content.res.XModuleResources;
import android.content.res.XResources;
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
            throw new AssertionError("Could not find " + resType + ": " + resName);
        }
        return resId;
    }

    public String getString(String resName) {
        int resId = getResId(resName, "string");
        return mModuleRes.getString(resId);
    }

    public XmlPullParser getLayout(String resName) {
        int resId = getResId(resName, "layout");
        return mModuleRes.getLayout(resId);
    }
}
