/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author zqq90
 */
public class EncryptUtil {

    private static String DIGEST(String name, byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes == null) {
            return null;
        }
        final MessageDigest md = MessageDigest.getInstance(name);
        md.update(bytes);
        return StringUtil.HEX(md.digest());
    }

    private static String digest(String name, byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes == null) {
            return null;
        }
        final MessageDigest md = MessageDigest.getInstance(name);
        md.update(bytes);
        return StringUtil.hex(md.digest());
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
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
