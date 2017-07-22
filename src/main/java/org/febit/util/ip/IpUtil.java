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
package org.febit.util.ip;

import java.util.regex.Pattern;

/**
 *
 * @author zqq90
 */
public class IpUtil {

    public static final long IP_MAX = 0xFFFFFFFFL;
    public static final int IP_MAX_INT = 0xFFFFFFFF;
    protected static Pattern REGX_IP = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");

    public static long parseLong(final String ip) {
        return ipv4ToLong(ip);
    }

    /**
     * IPv4 to Long.
     *
     * @param ipv4
     * @return -lL if not a ip.
     */
    public static long ipv4ToLong(final String ipv4) {
        if (ipv4 == null) {
            return -1L;
        }
        final int length = ipv4.length();
        if (length < 7 || length > 15) {
            return -1L;
        }

        char[] chars = new char[length + 1];
        chars[length] = '.';
        ipv4.getChars(0, length, chars, 0);

        int i = 0;

        int pieceCount = 0;
        long result = 0;
        int piece = 0;
        while (i <= length) {
            char c = chars[i++];
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    piece = piece * 10 + c - '0';
                    continue;
                case '.':
                    pieceCount++;
                    if (pieceCount > 4) {
                        return -1L;
                    }
                    if ((piece | 0xFF) != 0xFF) {
                        return -1L;
                    }
                    result = (result << 8) + piece;
                    piece = 0;
                    continue;
                default:
                    return -1L;
            }
        }
        return result;
    }

    public static boolean isIpv4(String ipv4) {
        if (ipv4 == null) {
            return false;
        }
        return REGX_IP.matcher(ipv4).matches();
    }

    public static int parseInt(final String ipv4) {

        final int position1 = ipv4.indexOf('.');
        final int position2 = ipv4.indexOf('.', position1 + 1);
        final int position3 = ipv4.indexOf('.', position2 + 1);

        return ((Integer.parseInt(ipv4.substring(0, position1)) & 0xFF) << 24)
                | ((Integer.parseInt(ipv4.substring(position1 + 1, position2)) & 0xFF) << 16)
                | ((Integer.parseInt(ipv4.substring(position2 + 1, position3)) & 0xFF) << 8)
                | ((Integer.parseInt(ipv4.substring(position3 + 1)) & 0xFF));
    }

    public static byte[] parseBytes(final String ipv4) {

        final int position1 = ipv4.indexOf('.');
        final int position2 = ipv4.indexOf('.', position1 + 1);
        final int position3 = ipv4.indexOf('.', position2 + 1);

        final byte[] ret = new byte[4];

        ret[0] = (byte) Integer.parseInt(ipv4.substring(0, position1));
        ret[1] = (byte) Integer.parseInt(ipv4.substring(position1 + 1, position2));
        ret[2] = (byte) Integer.parseInt(ipv4.substring(position2 + 1, position3));
        ret[3] = (byte) Integer.parseInt(ipv4.substring(position3 + 1));
        return ret;
    }

    public static String toString(final long ip) {
        return IpUtil.toString((int) ip);
    }

    public static String toString(final int ip) {
        return Integer.toString(((ip >> 24) & 0xFF)) + '.'
                + ((ip >> 16) & 0xFF) + '.'
                + ((ip >> 8) & 0xFF) + '.'
                + ((ip & 0xFF));
    }

    public static long makeLong(final byte b0, final byte b1, final byte b2, final byte b3) {
        return int2long(makeInt(b0, b1, b2, b3));
    }

    public static int makeInt(final byte b0, final byte b1, final byte b2, final byte b3) {
        return ((b0 & 0xFF) << 24)
                | ((b1 & 0xFF) << 16)
                | ((b2 & 0xFF) << 8)
                | ((b3 & 0xFF));
    }

    public static int long2int(final long i) {
        return (int) i;
    }

    public static String getSegmentStart(final String ip, final int mark) {
        return toString(getSegmentStart(parseLong(ip), mark));
    }

    public static int getSegmentStart(final int ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip & (IP_MAX_INT << i);
    }

    public static long getSegmentStart(final long ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip & (IP_MAX << i);
    }

    public static String getSegmentEnd(final String ip, final int mark) {
        return toString(getSegmentEnd(parseLong(ip), mark));
    }

    public static int getSegmentEnd(final int ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip | (1 << (i - 1));
    }

    public static long getSegmentEnd(final long ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip | ((1L << i) - 1);
    }

    public static long int2long(final int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}
