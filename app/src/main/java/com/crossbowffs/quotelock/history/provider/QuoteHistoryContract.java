package com.crossbowffs.quotelock.history.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.crossbowffs.quotelock.BuildConfig;

/**
 * @author Yubyf
 */
public class QuoteHistoryContract {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".history.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Histories implements BaseColumns {
        public static final String TABLE = "histories";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(QuoteHistoryContract.CONTENT_URI, TABLE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.crossbowffs.history";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.crossbowffs.history";

        public static final String MD5 = "md5";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String[] ALL = {
                _ID,
                MD5,
                TEXT,
                SOURCE
        };
    }
}
