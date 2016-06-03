package com.crossbowffs.quotelock.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
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
