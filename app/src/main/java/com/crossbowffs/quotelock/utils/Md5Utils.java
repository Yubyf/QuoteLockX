package com.crossbowffs.quotelock.utils;

import androidx.annotation.Size;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Yubyf
 * @date 5/5/21.
 */
public class Md5Utils {

    @Size(16)
    public static byte[] calculateFileMd5(File file) throws Exception {
        InputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String calculateFileMd5Str(String filename) throws Exception {
        return md5BytesToStr(calculateFileMd5(new File(filename)));
    }

    public static String calculateFileMd5Str(File file) throws Exception {
        return md5BytesToStr(calculateFileMd5(file));
    }

    @Size(32)
    public static String md5BytesToStr(@Size(16) byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) {
            result.append(Integer.toHexString((0x000000FF & value) | 0xFFFFFF00).substring(6));
        }
        return result.toString();
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
