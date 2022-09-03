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

import jodd.net.URLDecoder;
import org.febit.lang.util.Maps;
import org.febit.lang.util.StringWalker;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class HttpUtil {

    public static final String KEY_URI_PATH = "&&path";

    protected static final StringWalker.Checker CHECKER_URI_PATH_END = c -> c == '?' || c == '&' || c == '#';
    protected static final StringWalker.Checker CHECKER_QUERY_NAME_END = c -> c == '=' || c == '&' || c == '#';
    protected static final StringWalker.Checker CHECKER_QUERY_VALUE_END = c -> c == '&' || c == '#';

    public static Map<String, String> parseCookies(final String raw) {
        if (raw == null
                || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> ret = Maps.create(16);
        final StringWalker walker = new StringWalker(raw);
        //Step: k-v
        while (!walker.isEnd()) {
            walker.skipBlanks();
            if (walker.isEnd()) {
                break;
            }
            ret.put(walker.readTo('=', false),
                    walker.readTo(';', false));
        }
        return ret;
    }

    public static Map<String, String> parseUriQuerys(final String uriRaw) {
        return parseUriQuerys(uriRaw, false);
    }

    public static Map<String, String> parseUriQuerys(final String src, final boolean withPath) {
        if (src == null
                || src.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> ret = Maps.create(16);
        final StringWalker walker = new StringWalker(src);

        //Step: path
        String path = walker.readToFlag(CHECKER_URI_PATH_END, false);
        if (withPath) {
            ret.put(KEY_URI_PATH, path);
        }

        //Step: k-v
        parseQuerys(walker, ret);
        return ret;
    }

    public static Map<String, String> parseQuerys(String src) {
        if (src == null || src.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> ret = Maps.create(16);
        final StringWalker walker = new StringWalker(src);
        //Step: k-v
        parseQuerys(walker, ret);
        return ret;
    }

    protected static void parseQuerys(StringWalker walker, Map<String, String> result) {
        //Step: k-v
        while (true) {
            //skip DUP &
            walker.skipChar('&');
            if (walker.isEnd() || walker.peek() == '#') {
                break;
            }
            String name = walker.readToFlag(CHECKER_QUERY_NAME_END, true);
            if (!walker.isEnd() && walker.peek() != '#') {
                walker.jump(1);
            }
            String value = decodeUri(walker.readToFlag(CHECKER_QUERY_VALUE_END, true));
            if (!walker.isEnd() && walker.peek() != '#') {
                walker.jump(1);
            }
            result.put(name, value);
        }
    }

    public static String decodeUri(String src) {
        if (StringUtil.isEmpty(src)) {
            return src;
        }
        try {
            return URLDecoder.decode(src, "UTF-8");
        } catch (Exception e) {
            return src;
        }
    }
}
