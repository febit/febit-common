package org.febit.util;

import java.util.Collections;
import java.util.Map;
import jodd.util.URLDecoder;

/**
 *
 * @author zqq90
 */
public class HttpUtil {

    public static final String KEY_URI_PATH = "&&path";

    public static Map<String, String> parseCookies(final String raw) {
        if (raw == null
                || raw.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        final Map<String, String> ret = CollectionUtil.createMap(16);
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

    protected static final StringWalker.Checker CHECKER_URI_PATH_END = new StringWalker.Checker() {
        @Override
        public boolean isFlag(char c) {
            return c == '?' || c == '&' || c == '#';
        }
    };

    protected static final StringWalker.Checker CHECKER_QUERY_NAME_END = new StringWalker.Checker() {
        @Override
        public boolean isFlag(char c) {
            return c == '=' || c == '&' || c == '#';
        }
    };
    protected static final StringWalker.Checker CHECKER_QUERY_VALUE_END = new StringWalker.Checker() {
        @Override
        public boolean isFlag(char c) {
            return c == '&' || c == '#';
        }
    };

    public static Map<String, String> parseUriQuerys(final String uriRaw) {
        return parseUriQuerys(uriRaw, false);
    }

    public static Map<String, String> parseUriQuerys(final String src, final boolean withPath) {
        if (src == null
                || src.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        final Map<String, String> ret = CollectionUtil.createMap(16);
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
            return Collections.EMPTY_MAP;
        }
        final Map<String, String> ret = CollectionUtil.createMap(16);
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
