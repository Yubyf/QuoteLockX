package com.crossbowffs.quotelock.modules.custom.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import com.crossbowffs.quotelock.BuildConfig;

public class CustomQuoteContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".modules.custom.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Quotes implements BaseColumns {
        public static final String TABLE = "quotes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(CustomQuoteContract.CONTENT_URI, TABLE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.crossbowffs.quote";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.crossbowffs.quote";

        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String[] ALL = {
            _ID,
            TEXT,
            SOURCE
        };
    }
}
