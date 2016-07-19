// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import jodd.util.URLDecoder;
import jodd.util.UnsafeUtil;
import jodd.util.buffer.FastCharBuffer;
import org.febit.lang.Defaults;

/**
 *
 * @author zqq90
 */
public class StringUtil extends jodd.util.StringUtil {

    private static final char[] GT = "&gt;".toCharArray();
    private static final char[] LT = "&lt;".toCharArray();

    public static String urlEncode(String src) {
        try {
            return URLEncoder.encode(src, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return src;
        }
    }

    public static String urlDecode(String src) {
        return URLDecoder.decode(src, "UTF-8");
    }

    public static String textBetween(String src, String start, String end) {
        if (src == null) {
            return null;
        }
        int from = src.indexOf(start);
        if (from < 0) {
            return null;
        }
        from += start.length();
        int to = src.indexOf(end, from);
        if (to < 0) {
            return null;
        }
        return src.substring(from, to);
    }

    final static char[] fulldigits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    public static String toHalfString(int i) {
        char[] buf = new char[13];
        int charPos = 12;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -36) {
            buf[charPos--] = fulldigits[(int) (-(i % 36))];
            i = i / 36;
        }
        buf[charPos] = fulldigits[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        while (charPos > 9) {
            buf[--charPos] = '0';
        }

        return new String(buf, charPos, (13 - charPos));
    }

    public static String toMinString(long i) {
        char[] buf = new char[13];
        int charPos = 12;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -62) {
            buf[charPos--] = fulldigits[(int) (-(i % 62))];
            i = i / 62;
        }
        buf[charPos] = fulldigits[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (13 - charPos));
    }

    public static String toString(final FastCharBuffer buffer) {
        return UnsafeUtil.createString(buffer.toArray());
    }

    public static String concat(final String s1, final String s2) {
        return s1.concat(s2);
    }

    public static String concat(String string, int number) {
        return string.concat(Integer.toString(number));
    }

    public static String concat(String... strings) {
        int i;
        final int size = strings.length;
        final StringBuilder sb = new StringBuilder(size * 16);
        for (i = 0; i < size; i++) {
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    public static String concat(final String s1, final String s2, final String s3) {
        return new StringBuilder(s1.length() + s2.length() + s3.length())
                .append(s1)
                .append(s2)
                .append(s3).toString();
    }

    public static String concat(String s1, String s2, String s3, String s4) {
        return new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length())
                .append(s1)
                .append(s2)
                .append(s3)
                .append(s4).toString();
    }

    /**
     * 全角转半角
     *
     * @param input String.
     * @return 半角字符串
     */
    public static String toDBC(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        final char arr[] = input.toCharArray();
        final int len = arr.length;
        boolean changed = false;
        for (int i = 0; i < len; i++) {
            final char c = arr[i];
            if (c > '\uFF00' && c < '\uFF5F') {
                arr[i] = (char) (c - 65248);
                changed = true;
                continue;
            }
            if (c == '\u3000') {
                arr[i] = ' ';
                changed = true;
                continue;
            }
        }
        return changed ? new String(arr) : input;
    }

    public static String toTrimedUpperDBC(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return toDBC(input).trim().toUpperCase();
    }

    private static boolean match(Pattern pattern, String string) {
        return string != null && pattern.matcher(string).matches();
    }

    public static String[] toArrayExcludeCommit(String src) {
        final String[] array = toArray(src);
        int count = 0;
        for (String str : array) {
            if (str.charAt(0) != '#') {
                array[count++] = str;
            }
        }
        if (count == 0) {
            return Defaults.EMPTY_STRINGS;
        }
        if (count != array.length) {
            return ArraysUtil.subarray(array, 0, count);
        }
        return array;
    }

    /**
     * split with ',' '\n' '\r', and trimed, exclude EMPTY string
     *
     * @param src
     * @return 如果是null 返回空数组而不是null
     */
    public static String[] toArray(String src) {
        if (src == null || src.isEmpty()) {
            return Defaults.EMPTY_STRINGS;
        }

        final char[] srcc = src.toCharArray();
        final int len = srcc.length;

        // list max size = (size + 1) / 2
        final List<String> list = new ArrayList<>(
                len > 1024 ? 128
                        : len > 64 ? 32
                                : (len + 1) >> 1);

        int i = 0;
        while (i < len) {
            //skip empty & splits
            while (i < len) {
                char c = srcc[i];
                if (c != ','
                        && c != '\n'
                        && c != '\r'
                        && c != '，'
                        && c != ' '
                        && c != '\t') {
                    break;
                }
                i++;
            }
            //check if end
            if (i == len) {
                break;
            }
            final int start = i;

            //find end
            while (i < len) {
                char c = srcc[i];
                if (c == ','
                        || c == '\n'
                        || c == '\r'
                        || c == '，') {
                    break;
                }
                i++;
            }
            int end = i;
            //trim back end
            for (;;) {
                char c = srcc[end - 1];
                if (c == ' '
                        || c == '\t') {
                    end--;
                    continue;
                }
                break;
            }
            list.add(new String(srcc, start, end - start));
        }
        if (list.isEmpty()) {
            return Defaults.EMPTY_STRINGS;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 替换 字符串中的 {n}
     *
     * @param template
     * @param array
     * @return
     */
    public static String format(final String template, final Object... array) {
        if (template == null) {
            return null;
        }
        if (template.indexOf('{') < 0) {
            return template;
        }
        final StringBuilder result = new StringBuilder(template.length());
        final int len = template.length();
        final int arrayLen = array != null ? array.length : 0;
        int i = 0;
        int currentIndex = 0;
        int index;
        while (i < len) {
            int ndx = template.indexOf('{', i);
            if (ndx == -1) {
                result.append(i == 0 ? template : template.substring(i));
                break;
            }
            int j = ndx - 1;
            while ((j >= 0) && (template.charAt(j) == '\\')) {
                j--;
            }
            int escapeCharcount = ndx - 1 - j;
            result.append(template.substring(i,
                    escapeCharcount > 0
                            ? ndx - ((escapeCharcount + 1) >> 1)
                            : ndx));
            if ((escapeCharcount & 1) == 1) {
                result.append('{');
                i = ndx + 1;
                continue;
            }
            ndx += 1;
            int ndxEnd = template.indexOf('}', ndx);
            if (ndxEnd == -1) {
                throw new IllegalArgumentException(StringUtil.concat("Invalid message, unclosed macro at: ", ndx - 1));
            }
            if (ndx == ndxEnd) {
                index = currentIndex++;
            } else {
                index = template.charAt(ndx) - '0';
                for (int k = ndx + 1; k < ndxEnd; k++) {
                    index = index * 10 + (template.charAt(k) - '0');
                }
            }
            if (index < arrayLen && index >= 0 && array[index] != null) {
                result.append(array[index].toString());
            }
            i = ndxEnd + 1;
        }
        return result.toString();
    }

    public static boolean isMobile(String string) {
        if (string == null) {
            return false;
        }
        if (string.length() != 11) {
            return false;
        }
        if (string.charAt(0) != '1') {
            return false;
        }
        return isNumeric(string);
    }

    public static boolean isNumber(String string) {
        if (string == null) {
            return false;
        }
        int size = string.length();
        if (size == 0) {
            return false;
        }
        char c;
        boolean dotFlag = false;
        for (int i = 0; i < size; i++) {
            c = string.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c == '.') {
                if (dotFlag) {
                    return false;
                }
                dotFlag = true;
                continue;
            }
            if (i == 0 && c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isAllNumeric(String... array) {
        for (String string : array) {
            if (!isNumeric(string)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumeric(String string) {
        if (string == null) {
            return false;
        }
        int size = string.length();
        if (size == 0) {
            return false;
        }
        char c;
        for (int i = 0; i < size; i++) {
            if ((c = string.charAt(i)) < '0'
                    || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphaNumeric(String string) {
        if (string == null) {
            return false;
        }
        int size = string.length();
        char c;
        for (int i = 0; i < size; i++) {
            c = string.charAt(i);
            if (!(((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlpha(String string) {
        if (string == null) {
            return false;
        }
        int size = string.length();
        for (int i = 0; i < size; i++) {
            char c = string.charAt(i);
            if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))) {
                return false;
            }
        }
        return true;
    }
    private static final Pattern pattern_Emailaddr = Pattern.compile("[_a-zA-Z0-9.-]+@[_a-zA-Z0-9.-]+\\.[a-zA-Z0-9]{2,4}");

    public static boolean isEmailAddr(String string) {
        return match(pattern_Emailaddr, string);
    }

    public static String randomUUID() {
        return StringUtil.remove(UUID.randomUUID().toString(), '-');
    }

    public static String strickFileName(final String str) {
        if (str != null) {
            return StringUtil.removeChars(str, ' ', '\t', '\f', '\n', '\r', '\b', '\\', '/', '<', '>', '*', '?', ':', '"', '|').trim();
        }
        return null;
    }

    public static String escapeFileName(final String str) {
        if (str != null) {
            return StringUtil.removeChars(str, '\t', '\f', '\n', '\r', '\b', '\\', '/', '<', '>', '*', '?', ':', '"', '|').trim();
        }
        return null;
    }

    public static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final char[] ASCII_CHARS = {'0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6',
        '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F', '1', '0', '1',
        '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1', 'A', '1', 'B',
        '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2',
        '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2', 'F',};

    public static String HEX(final byte[] bs) {
        return toHexString(bs, DIGITS_UPPER);
    }

    public static String hex(final byte[] bs) {
        return toHexString(bs, DIGITS);
    }

    public static String toHexString(final byte[] bs) {
        return toHexString(bs, DIGITS);
    }

    public static String toHexString(final byte[] bs, final char[] myDigits) {
        final int len;
        if (bs != null && (len = bs.length) != 0) {
            final char[] cs = new char[len << 1];
            byte b;
            for (int i = 0, j = 0; i < len; i++) {
                cs[j++] = myDigits[((b = bs[i]) >>> 4) & 0xF];
                cs[j++] = myDigits[b & 0xF];
            }
            return String.valueOf(cs);
        }
        return null;
    }

    public static int charTodigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        throw new IllegalArgumentException("must [0-9a-zA-Z]");
    }

    public static byte[] hexToBytes(final String strIn) {
        if (strIn != null) {
            final int len;
            final char[] chars = strIn.toCharArray();
            final byte[] out = new byte[len = (chars.length / 2)];
            //int h,l;
            for (int i = 0, j = 0; i < len; i++) {
                //h = char2digit(chars[j++]) << 4;
                //l = char2digit(chars[j++]);
                out[i] = (byte) (charTodigit(chars[j++]) << 4 | charTodigit(chars[j++]));
            }
            return out;
        }
        return null;
    }

    public static String escapeForJsonString(String src, boolean wrap) {
        final int len;
        if (src == null || (len = src.length()) == 0) {
            return src;
        }
        final StringBuilder sb = new StringBuilder(len + 16);
        if (wrap) {
            sb.append('"');
        }

        char ch;
        char replace = '\0';
        for (int i = 0; i < len; i++) {
            ch = src.charAt(i);
            switch (ch) {
                case '\b':
                    replace = 'b';
                    break;
                case '\f':
                    replace = 'f';
                    break;
                case '\n':
                    replace = 'n';
                    break;
                case '\r':
                    replace = 'r';
                    break;
                case '\t':
                    replace = 't';
                    break;
                case '"':
                    replace = '"';
                    break;
                case '/':
                    replace = '/';
                    break;
                case '\\':
                    replace = '\\';
                    break;
            }
            if (replace != '\0') {
                sb.append('\\').append(replace);
                replace = '\0';
                continue;
            }
            sb.append(ch);
        }
        if (wrap) {
            sb.append('"');
        }
        return sb.toString();
    }

    public static String escapeRegex(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        StringBuilder buffer = new StringBuilder(src.length() * 3 / 2);
        escapeRegex(buffer, src);
        return buffer.toString();
    }

    public static void escapeRegex(final StringBuilder buffer, String src) {
        if (src == null || src.isEmpty()) {
            return;
        }
        for (char c : src.toCharArray()) {
            switch (c) {
                case '*':
                case '.':
                case '?':
                case '+':
                case '$':
                case '^':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '|':
                case '\\':
                case '/':
                    buffer.append('\\');
            }
            buffer.append(c);
        }
    }

    public static String escapeUTF8(String src) {
        return escapeUTF8(src, false);
    }

    public static String escapeUTF8(String src, boolean wrap) {
        if (src == null) {
            return null;
        }
        final int len;
        if ((len = src.length()) == 0) {
            return wrap ? "\"\"" : "";
        }
        final StringBuilder sb = new StringBuilder(len << 1);
        try {
            escapeUTF8(src, sb, wrap);
        } catch (IOException ignore) {
        }
        return sb.toString();
    }

    public static void escapeUTF8(final String src, final StringBuilder buffer, boolean wrap) throws IOException {
        if (src == null) {
            return;
        }
        final int len;
        if ((len = src.length()) == 0) {
            if (wrap) {
                buffer.append("\"\"");
            }
            return;
        }
        if (wrap) {
            buffer.append('"');
        }
        //char[] chars = src.toCharArray();
        char[] buf = new char[6];
        buf[0] = '\\';
        buf[1] = 'u';

        final char[] digits = StringUtil.DIGITS_UPPER;
        char ch;
        char replace = '\0';
        for (int i = 0; i < len; i++) {
            ch = src.charAt(i);
            switch (ch) {
                case '\b':
                    replace = 'b';
                    break;
                case '\f':
                    replace = 'f';
                    break;
                case '\n':
                    replace = 'n';
                    break;
                case '\r':
                    replace = 'r';
                    break;
                case '\t':
                    replace = 't';
                    break;
                case '"':
                    replace = '"';
                    break;
                case '/':
                    replace = '/';
                    break;
                case '\\':
                    replace = '\\';
                    break;
            }

            if (replace != '\0') {
                buffer.append('\\').append(replace);
                replace = '\0';
                continue;
            }

            if (ch < 32) {
                buf[2] = '0';
                buf[3] = '0';
                buf[4] = ASCII_CHARS[ch << 1];
                buf[5] = ASCII_CHARS[ch << 2 + 1];
                buffer.append(buf, 0, 6);
                continue;
            }

            if (ch >= 127) {
                buf[2] = digits[(ch >>> 12) & 15];
                buf[3] = digits[(ch >>> 8) & 15];
                buf[4] = digits[(ch >>> 4) & 15];
                buf[5] = digits[ch & 15];
                buffer.append(buf, 0, 6);
                continue;
            }
            buffer.append(ch);
        }
        if (wrap) {
            buffer.append('"');
        }
    }

    public static String cutPrefix(String string, String prefix) {
        if (string.startsWith(prefix)) {
            string = string.substring(prefix.length());
        }
        return string;
    }

    public static String cutSuffix(String string, String suffix) {
        if (string.endsWith(suffix)) {
            string = string.substring(0, string.length() - suffix.length());
        }
        return string;
    }

    public static final String EMPTY = "";

    public static String upperFirst(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return CharUtil.toUpperAscii(str.charAt(0)) + str.substring(1);
    }

    public static String lowerFirst(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return CharUtil.toLowerAscii(str.charAt(0)) + str.substring(1);
    }

    /**
     * 是否是 半角字符
     *
     * @param c
     * @return
     */
    public static boolean isDbcCase(char c) {
        if (c <= 127) {
            return true;
        }
        // 日文半角片假名和符号
        if (c >= 65377 && c <= 65439) {
            return true;
        }
        return false;
    }

    /**
     * 按 1全角=2半角 计算长度
     *
     * @param value
     * @return
     */
    public static int getDbcLength(String value) {
        int len = value.length();
        int result = len;
        for (int i = 0; i < len; i++) {
            if (!isDbcCase(value.charAt(i))) {
                result++;
            }
        }
        return result;
    }

    /**
     * 按 1全角=2半角 计算长度
     *
     * @param value
     * @param maxLen 最大长度
     * @return
     */
    public static int getDbcLength(String value, final int maxLen) {
        int len = value.length();
        int result = len;
        for (int i = 0; i < len && result < maxLen; i++) {
            if (!isDbcCase(value.charAt(i))) {
                result++;
            }
        }
        return result <= maxLen ? result : maxLen;
    }

    public static String escapeHTMLTag(String str) {
        if (str != null) {
            final char[] source = UnsafeUtil.getChars(str);
            final int size = source.length;
            final StringBuilder buffer = new StringBuilder(str.length() + (size > 1000 ? 200 : 100));
            for (int i = 0; i < size; i++) {
                char c = source[i];
                if (c == '<') {
                    buffer.append(LT);
                } else if (c == '>') {
                    buffer.append(GT);
                } else {
                    buffer.append(c);
                }
            }
            return buffer.toString();
        }
        return null;
    }

    public static String join(Iterator list, char separator) {
        if (list == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean notfirst = false;
        while (list.hasNext()) {
            Object item = list.next();
            if (notfirst) {
                sb.append(separator);
            } else {
                notfirst = true;
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static String join(Iterator list, String separator) {
        if (list == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean notfirst = false;
        while (list.hasNext()) {
            Object item = list.next();
            if (notfirst) {
                sb.append(separator);
            } else {
                notfirst = true;
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static String join(Collection list, char separator) {
        if (list == null) {
            return "";
        }
        int size = list.size();
        if (size == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean notfirst = false;
        for (Object item : list) {
            if (notfirst) {
                sb.append(separator);
            } else {
                notfirst = true;
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static String join(Collection list, String separator) {

        if (list == null) {
            return "";
        }
        int size = list.size();
        if (size == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean notfirst = false;
        for (Object item : list) {
            if (notfirst) {
                sb.append(separator);
            } else {
                notfirst = true;
            }
            sb.append(item);
        }
        return sb.toString();
    }
    
    public static String cutToLastIndexOf(String string, String substring) {
		int i = string.lastIndexOf(substring);
		if (i <0) {
			return "";
		}
		return string.substring(0, i);
	}
    
	public static String cutToLastIndexOf(String string, char c) {
		int i = string.lastIndexOf(c);
		if (i <0) {
			return "";
		}
		return string.substring(0, i);
	}

	public static String cutFromLastIndexOf(String string, String substring) {
		int i = string.lastIndexOf(substring);
		if (i != -1) {
			string = string.substring(i);
		}
		return string;
	}
    
	public static String cutFromLastIndexOf(String string, char c) {
		int i = string.lastIndexOf(c);
		if (i != -1) {
			string = string.substring(i);
		}
		return string;
	}

}
