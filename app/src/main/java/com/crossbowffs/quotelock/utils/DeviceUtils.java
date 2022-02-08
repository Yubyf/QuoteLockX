package com.crossbowffs.quotelock.utils;

import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {

    /** 1900/1905:OP7 China, 1910 OP7Pro China, 1911: India, 1913: EU, 1915: Tmobile, 1917: global/US unlocked, 1920: EU 5G */
    private static final List<String> OP7_DEVICE_MODELS = new ArrayList<String>() {{
        add("GM1900");
        add("GM1905");
        add("GM1910");
        add("GM1911");
        add("GM1913");
        add("GM1915");
        add("GM1917");
        add("GM1920");
    }};

    /**
     * @return True if current device belongs to OnePlus 7 series.
     */
    public static boolean isOnePlus7Series() {
        return OP7_DEVICE_MODELS.contains(android.os.Build.MODEL);
    }
}
