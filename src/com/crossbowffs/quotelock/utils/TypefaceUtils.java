package com.crossbowffs.quotelock.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

import androidx.annotation.FontRes;
import androidx.core.content.res.ResourcesCompat;

/**
 * @author Yubyf
 * @date 2021/5/6.
 */
public class TypefaceUtils {

    public static Typeface getFont(Context context, @FontRes int id) {
        return ResourcesCompat.getFont(context, id);
    }

    public static Typeface getFont(Context context, String fontResName) {
        Resources res = context.getResources();
        int resId = res.getIdentifier(fontResName, "font", context.getPackageName());
        if (resId == 0) {
            throw new Resources.NotFoundException("Could not find font: " + fontResName);
        }
        return getFont(context, resId);
    }
}
