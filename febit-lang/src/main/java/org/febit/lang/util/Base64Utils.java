/*
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
package org.febit.lang.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class Base64Utils {

    public static String encode(byte[] src) {
        if (src.length == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(src);
    }

    public static String encode(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return encode(src.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decode(String src) {
        if (src.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(src);
    }

    public static String decodeToString(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return new String(decode(src), StandardCharsets.UTF_8);
    }

    public static String encodeUrlSafe(byte[] src) {
        return Base64.getUrlEncoder().encodeToString(src);
    }

    public static String encodeUrlSafe(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return encodeUrlSafe(src.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeUrlSafe(String src) {
        return Base64.getUrlDecoder().decode(src);
    }

    public static String decodeUrlSafeToString(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return new String(decodeUrlSafe(src), StandardCharsets.UTF_8);
    }

    public static String encodeMime(byte[] src) {
        return Base64.getMimeEncoder().encodeToString(src);
    }

    public static String encodeMime(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return encodeMime(src.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeMime(String src) {
        return Base64.getMimeDecoder().decode(src);
    }

    public static String decodeMimeToString(String src) {
        if (src.isEmpty()) {
            return "";
        }
        return new String(decodeMime(src), StandardCharsets.UTF_8);
    }

}
