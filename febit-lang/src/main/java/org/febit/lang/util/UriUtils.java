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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class UriUtils {

    private static final char SLASH = '/';

    public static String encode(String src) {
        return URLEncoder.encode(src, StandardCharsets.UTF_8);
    }

    public static String decode(String src) {
        return URLDecoder.decode(src, StandardCharsets.UTF_8);
    }

    public static String concat(String base, String... appends) {
        var bufSize = base.length() + appends.length;
        for (var append : appends) {
            bufSize += append.length();
        }
        var buf = new StringBuilder(bufSize);
        buf.append(base);

        for (var append : appends) {
            if (buf.charAt(buf.length() - 1) != SLASH) {
                buf.append(SLASH);
            }
            if (append.isEmpty()) {
                continue;
            }
            if (append.charAt(0) == SLASH) {
                buf.append(append, 1, append.length());
            } else {
                buf.append(append);
            }
        }
        return buf.toString();
    }
}
