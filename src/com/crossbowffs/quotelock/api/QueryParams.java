package com.crossbowffs.quotelock.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.HashMap;

public class QueryParams {
    private final HashMap<String, Object> mParams = new HashMap<>();

    protected void setParam(String key, Object value) {
        mParams.put(key, value);
    }

    protected void setArrayParam(String key, Object value) {
        mParams.put(key, value);
    }

    public String buildUrl(String baseUrl) {
        try {
            if (mParams.size() == 0) {
                return baseUrl;
            }
            StringBuilder sb = new StringBuilder("?");
            for (HashMap.Entry<String, Object> entry : mParams.entrySet()) {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append('=');
                if (entry.getValue().getClass().isArray()) {
                    Object valueArr = entry.getValue();
                    int length = Array.getLength(valueArr);
                    if (length != 0) {
                        for (int i = 0; i < length; ++i) {
                            Object o = Array.get(valueArr, i);
                            sb.append(URLEncoder.encode(o.toString(), "UTF-8"));
                            sb.append(',');
                        }
                        sb.setLength(sb.length() - 1);
                    }
                } else {
                    sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                }
                sb.append('&');
            }
            sb.setLength(sb.length() - 1);
            return baseUrl + sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
