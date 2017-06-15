// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author zqq90
 */
public class EncryptUtil {

    private static String DIGEST(String name, byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes != null) {
            final MessageDigest md = MessageDigest.getInstance(name);
            md.update(bytes);
            return StringUtil.HEX(md.digest());
        }
        return null;
    }

    private static String digest(String name, byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes != null) {
            final MessageDigest md = MessageDigest.getInstance(name);
            md.update(bytes);
            return StringUtil.hex(md.digest());
        }
        return null;
    }

    public static String SHA1(byte[] bytes) {
        try {
            return DIGEST("SHA-1", bytes);
        } catch (NoSuchAlgorithmException bytesz) {
        }
        return null;
    }

    public static String SHA1(String str) {
        if (str == null) {
            return null;
        }
        return SHA1(toBytes(str));
    }

    public static String sha1(byte[] bytes) {
        try {
            return digest("SHA-1", bytes);
        } catch (NoSuchAlgorithmException bytesz) {
        }
        return null;
    }

    public static String sha1(String str) {
        if (str == null) {
            return null;
        }
        return sha1(toBytes(str));
    }

    public static String MD5(byte[] bytes) {
        try {
            return DIGEST("MD5", bytes);
        } catch (NoSuchAlgorithmException ignore) {
        }
        return null;
    }

    public static String MD5(String str) {
        if (str == null) {
            return null;
        }
        return MD5(toBytes(str));
    }

    public static String md5(byte[] bytes) {
        try {
            return digest("MD5", bytes);
        } catch (NoSuchAlgorithmException ignore) {
        }
        return null;
    }

    public static String md5(String str) {
        if (str == null) {
            return null;
        }
        return md5(toBytes(str));
    }

    protected static byte[] toBytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
