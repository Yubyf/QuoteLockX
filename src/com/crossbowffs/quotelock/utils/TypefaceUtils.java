package com.crossbowffs.quotelock.utils;

import android.content.Context;
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
}
