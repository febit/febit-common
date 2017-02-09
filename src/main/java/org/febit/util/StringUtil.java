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
public class StringUtil {

    private static final char[] GT = "&gt;".toCharArray();
    private static final char[] LT = "&lt;".toCharArray();

    public static boolean isEmpty(String src) {
        return src == null || src.isEmpty();
    }

    public static boolean isAllEmpty(String... strings) {
        for (String string : strings) {
            if (!isEmpty(string)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(String src) {
        return (src == null) || jodd.util.StringUtil.containsOnlyWhitespaces(src);
    }

    public static boolean isNotBlank(String src) {
        return (src != null) && !jodd.util.StringUtil.containsOnlyWhitespaces(src);
    }

    public static boolean isAllBlank(String... strings) {
        return jodd.util.StringUtil.isAllBlank(strings);
    }

    public static boolean isNotEmpty(String src) {
        return src != null && !src.isEmpty();
    }

    public static String remove(String s, String sub) {
        return jodd.util.StringUtil.remove(s, sub);
    }

    public static String removeChars(String src, String chars) {
        return jodd.util.StringUtil.removeChars(src, chars);
    }

    public static String removeChars(String src, char... chars) {
        return jodd.util.StringUtil.removeChars(src, chars);
    }

    public static String remove(String src, char ch) {
        return jodd.util.StringUtil.remove(src, ch);
    }

    public static String[] split(String src, String delimiter) {
        return jodd.util.StringUtil.split(src, delimiter);
    }

    public static String[] splitc(String src, String d) {
        return jodd.util.StringUtil.splitc(src, d);
    }

    public static String[] splitc(String src, char[] delimiters) {
        return jodd.util.StringUtil.splitc(src, delimiters);
    }

    public static String[] splitc(String src, char delimiter) {
        return jodd.util.StringUtil.splitc(src, delimiter);
    }

    public static int startsWithOne(String src, String[] dest) {
        return jodd.util.StringUtil.startsWithOne(src, dest);
    }

    public static int startsWithOneIgnoreCase(String src, String[] dest) {
        return jodd.util.StringUtil.startsWithOneIgnoreCase(src, dest);
    }

    public static String repeat(String src, int count) {
        return jodd.util.StringUtil.repeat(src, count);
    }

    public static String repeat(char c, int count) {
        return jodd.util.StringUtil.repeat(c, count);
    }

    public static String replace(String s, String sub, String with) {
        return jodd.util.StringUtil.replace(s, sub, with);
    }

    public static String replaceChar(String s, char sub, char with) {
        return jodd.util.StringUtil.replaceChar(s, sub, with);
    }

    public static String replaceChars(String s, char[] sub, char[] with) {
        return jodd.util.StringUtil.replaceChars(s, sub, with);
    }

    public static int count(String src, String sub) {
        return jodd.util.StringUtil.count(src, sub, 0);
    }

    public static int count(String src, String sub, int start) {
        return jodd.util.StringUtil.count(src, sub, start);
    }

    public static int count(String src, char c) {
        return jodd.util.StringUtil.count(src, c, 0);
    }

    public static int count(String src, char c, int start) {
        return jodd.util.StringUtil.count(src, c, start);
    }

    public static void trim(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string != null) {
                strings[i] = string.trim();
            }
        }
    }

    public static int indexOfChars(String src, String chars) {
        return jodd.util.StringUtil.indexOfChars(src, chars, 0);
    }

    public static int indexOfChars(String src, String chars, int startindex) {
        return jodd.util.StringUtil.indexOfChars(src, chars, startindex);
    }

    public static int indexOfChars(String src, char[] chars) {
        return jodd.util.StringUtil.indexOfChars(src, chars, 0);
    }

    public static int indexOfChars(String src, char[] chars, int startindex) {
        return jodd.util.StringUtil.indexOfChars(src, chars, startindex);
    }

    public static int indexOfIgnoreCase(String src, char c, int startIndex, int endIndex) {
        return jodd.util.StringUtil.indexOfIgnoreCase(src, c, startIndex, endIndex);
    }

    public static int indexOfIgnoreCase(String src, String subS) {
        return jodd.util.StringUtil.indexOfIgnoreCase(src, subS, 0, src.length());
    }

    public static int indexOfIgnoreCase(String src, String subS, int startIndex) {
        return jodd.util.StringUtil.indexOfIgnoreCase(src, subS, startIndex, src.length());
    }

    public static int indexOfIgnoreCase(String src, String sub, int startIndex, int endIndex) {
        return jodd.util.StringUtil.indexOfIgnoreCase(src, sub, startIndex, endIndex);
    }

    public static int lastIndexOfIgnoreCase(String s, String subS) {
        return jodd.util.StringUtil.lastIndexOfIgnoreCase(s, subS, s.length(), 0);
    }

    public static int lastIndexOfIgnoreCase(String src, String subS, int startIndex) {
        return jodd.util.StringUtil.lastIndexOfIgnoreCase(src, subS, startIndex, 0);
    }

    public static int lastIndexOfIgnoreCase(String src, String sub, int startIndex, int endIndex) {
        return jodd.util.StringUtil.lastIndexOfIgnoreCase(src, sub, startIndex, endIndex);
    }

    public static int lastIndexOfIgnoreCase(String src, char c, int startIndex, int endIndex) {
        return jodd.util.StringUtil.lastIndexOfIgnoreCase(src, c, startIndex, endIndex);
    }

    public static int lastIndexOf(String src, String sub, int startIndex, int endIndex) {
        return jodd.util.StringUtil.lastIndexOf(src, sub, startIndex, endIndex);
    }

    public static int lastIndexOf(String src, char c, int startIndex, int endIndex) {
        return jodd.util.StringUtil.lastIndexOf(src, c, startIndex, endIndex);
    }

    public static String cutPrefix(String src, String prefix) {
        if (src == null || !src.startsWith(prefix)) {
            return src;
        }
        return src.substring(prefix.length());
    }

    public static String cutSuffix(String src, String suffix) {
        if (src == null || !src.endsWith(suffix)) {
            return src;
        }
        return src.substring(0, src.length() - suffix.length());
    }

    public static String cutSurrounding(String src, String fix) {
        return jodd.util.StringUtil.cutSurrounding(src, fix, fix);
    }

    public static String cutSurrounding(String src, String prefix, String suffix) {
        return jodd.util.StringUtil.cutSurrounding(src, prefix, suffix);
    }

    public static String cutBetween(String src, String left, String right) {
        return jodd.util.StringUtil.cutBetween(src, left, right);
    }

    public static String cutTo(String src, String substring) {
        int i = src.indexOf(substring);
        if (i < 0) {
            return src;
        }
        return src.substring(0, i);
    }

    public static String cutTo(String src, char c) {
        int i = src.indexOf(c);
        if (i < 0) {
            return src;
        }
        return src.substring(0, i);
    }

    public static String cutFrom(String src, String substring) {
        int i = src.indexOf(substring);
        if (i < 0) {
            return "";
        }
        return src.substring(i);
    }

    public static String cutFrom(String src, char c) {
        int i = src.indexOf(c);
        if (i < 0) {
            return "";
        }
        return src.substring(i);
    }

    public static String cutToLast(String string, String substring) {
        int i = string.lastIndexOf(substring);
        if (i < 0) {
            return "";
        }
        return string.substring(0, i);
    }

    public static String cutToLast(String string, char c) {
        int i = string.lastIndexOf(c);
        if (i < 0) {
            return "";
        }
        return string.substring(0, i);
    }

    public static String cutFromLast(String string, String substring) {
        int i = string.lastIndexOf(substring);
        if (i != -1) {
            string = string.substring(i);
        }
        return string;
    }

    public static String cutFromLast(String string, char c) {
        int i = string.lastIndexOf(c);
        if (i != -1) {
            string = string.substring(i);
        }
        return string;
    }

    public static String encodeUri(String src) {
        try {
            return URLEncoder.encode(src, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return src;
        }
    }

    public static String decodeUri(String src) {
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

    final static char[] DIGITS_ALL = {
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
            buf[charPos--] = DIGITS_ALL[(int) (-(i % 36))];
            i = i / 36;
        }
        buf[charPos] = DIGITS_ALL[(int) (-i)];

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
            buf[charPos--] = DIGITS_ALL[(int) (-(i % 62))];
            i = i / 62;
        }
        buf[charPos] = DIGITS_ALL[(int) (-i)];

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
        final StringBuilder out = new StringBuilder(template.length());
        try {
            format(out, template, array);
        } catch (IOException ignore) {
            //can't be
        }
        return out.toString();
    }

    public static void format(final Appendable out, final String template, final Object... array) throws IOException {
        final int len = template.length();
        final int arrayLen = array != null ? array.length : 0;
        int i = 0;
        int currentIndex = 0;
        int index;
        while (i < len) {
            int ndx = template.indexOf('{', i);
            if (ndx == -1) {
                out.append(i == 0 ? template : template.substring(i));
                break;
            }
            int j = ndx - 1;
            while ((j >= 0) && (template.charAt(j) == '\\')) {
                j--;
            }
            int escapeCharcount = ndx - 1 - j;
            out.append(template.substring(i,
                    escapeCharcount > 0
                            ? ndx - ((escapeCharcount + 1) >> 1)
                            : ndx));
            if ((escapeCharcount & 1) == 1) {
                out.append('{');
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
                out.append(array[index].toString());
            }
            i = ndxEnd + 1;
        }
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
    private static final Pattern PATTERN_EMAIL = Pattern.compile("[_a-zA-Z0-9.-]+@[_a-zA-Z0-9.-]+\\.[a-zA-Z0-9]{2,4}");

    public static boolean isEmail(String string) {
        return match(PATTERN_EMAIL, string);
    }

    public static String randomUUID() {
        return StringUtil.remove(UUID.randomUUID().toString(), '-');
    }

    public static String strickFileName(final String str) {
        if (str == null) {
            return null;
        }
        return StringUtil.removeChars(str, FILENAME_FORBIDS).trim();
    }

    public static final char[] FILENAME_FORBIDS = {' ', '\t', '\f', '\n', '\r', '\b', '\\', '/', '<', '>', '*', '?', ':', '"', '|'};
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

    private static String toHexString(final byte[] bs, final char[] myDigits) {
        final int len;
        if (bs == null || (len = bs.length) == 0) {
            return null;
        }
        final char[] cs = new char[len << 1];
        byte b;
        for (int i = 0, j = 0; i < len; i++) {
            cs[j++] = myDigits[((b = bs[i]) >>> 4) & 0xF];
            cs[j++] = myDigits[b & 0xF];
        }
        return String.valueOf(cs);
    }

    public static byte[] decodeHex(final char[] chars) {
        final int len = (chars.length / 2);
        final byte[] out = new byte[len];
        for (int i = 0, j = 0; i < len; i++) {
            out[i] = (byte) (CharUtil.hexToDigit(chars[j++]) << 4 | CharUtil.hexToDigit(chars[j++]));
        }
        return out;
    }

    public static byte[] hexToBytes(final String strIn) {
        if (strIn == null) {
            return null;
        }
        return decodeHex(strIn.toCharArray());
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
            final char[] src = UnsafeUtil.getChars(str);
            final int size = src.length;
            final StringBuilder buffer = new StringBuilder(str.length() + (size > 1000 ? 200 : 100));
            for (int i = 0; i < size; i++) {
                char c = src[i];
                switch (c) {
                    case '<':
                        buffer.append(LT);
                        break;
                    case '>':
                        buffer.append(GT);
                        break;
                    default:
                        buffer.append(c);
                        break;
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

}
