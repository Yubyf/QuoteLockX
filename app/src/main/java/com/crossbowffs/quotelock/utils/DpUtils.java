package com.crossbowffs.quotelock.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * @author Yubyf
 */
public class DpUtils {

    public static float dp2px(Context context, float dip) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }
}
