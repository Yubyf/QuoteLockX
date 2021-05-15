package com.crossbowffs.quotelock.utils;

import androidx.annotation.Nullable;

import com.crossbowffs.quotelock.BuildConfig;
import com.crossbowffs.quotelock.consts.Urls;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class IOUtils {
    public static String streamToString(InputStream inputStream, String encoding, int bufferSize) throws IOException {
        char[] buffer = new char[bufferSize];
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(inputStream, encoding)) {
            while (true) {
                int count = reader.read(buffer, 0, bufferSize);
                if (count < 0) break;
                sb.append(buffer, 0, count);
            }
        }
        return sb.toString();
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        return streamToString(inputStream, "UTF-8", 2048);
    }

    public static String downloadString(String urlString) throws IOException {
        return downloadString(urlString, null);
    }

    public static String downloadString(String urlString, @Nullable Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        String ua = String.format("QuoteLock/%s (+%s)", BuildConfig.VERSION_NAME, Urls.GITHUB_QUOTELOCK);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestProperty("User-Agent",  ua);
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                }
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return streamToString(connection.getInputStream());
            } else {
                throw new IOException("Server returned non-200 status code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
}
