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
package org.febit.util.ip;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author zqq90
 */
public class IpUtil {

    public static final long IP_MAX = 0xFFFFFFFFL;
    public static final int IP_MAX_INT = 0xFFFFFFFF;
    protected final static Pattern REGX_IP = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");

    /**
     * IPv4 and IPv6 String to bytes.
     *
     * @param ip
     * @return null if invalid
     */
    public static byte[] ipToBytes(String ip) {
        byte[] ret = ipv4ToBytes(ip);
        if (ret != null) {
            return ret;
        }
        return ipv6ToBytes(ip);
    }

    /**
     * IPv4 String to bytes.
     *
     * @param ipv4
     * @return null if invalid
     */
    public static byte[] ipv4ToBytes(String ipv4) {
        if (ipv4 == null || ipv4.isEmpty()) {
            return null;
        }
        final String[] segments = ipv4.split("\\.", -1);
        if (segments.length != 4) {
            return null;
        }
        final byte[] ret = new byte[4];
        int val;
        for (int i = 0; i < 4; i++) {
            try {
                val = Integer.parseInt(segments[i]);
            } catch (NumberFormatException e) {
                return null;
            }
            if (val < 0 || val > 0xff) {
                return null;
            }
            ret[i] = (byte) (val & 0xff);
        }
        return ret;
    }

    /**
     * IPv6 String to bytes.
     *
     * @param ipv6
     * @return null if invalid
     */
    public static byte[] ipv6ToBytes(String ipv6) {
        if (ipv6 == null || ipv6.isEmpty()) {
            return null;
        }
        if (ipv6.charAt(0) == ':') {
            // 去除开头的第一个 ':'
            ipv6 = ipv6.substring(1);
        }
        final byte[] ret = new byte[16];
        final String segments[] = ipv6.split(":", 0);
        boolean mixedV4Flag = false;
        int pos = 15;
        // 倒序解析: 在压缩前，提前发现是否混合 IPv4
        for (int i = segments.length - 1; i >= 0; i--) {
            final String segment = segments[i];
            if (segment.indexOf('.') >= 0) {
                // 混合 ipv4
                byte[] temp = ipv4ToBytes(segment);
                if (temp == null) {
                    return null;
                }
                ret[pos--] = temp[3];
                ret[pos--] = temp[2];
                ret[pos--] = temp[1];
                ret[pos--] = temp[0];
                mixedV4Flag = true;
            } else if (segment.isEmpty()) {
                // 压缩区块 * 2
                // + 初始压缩个数为 1 个: 此区块占位
                // + 完整区块应该为 16/2=8 个
                // + 如果混合 ipv4 多一个: ipv4 相当于 2 个区块
                pos -= (1 + 8 - segments.length - (mixedV4Flag ? 1 : 0)) * 2;
            } else {
                int val;
                try {
                    val = Integer.parseInt(segment, 16);
                } catch (NumberFormatException e) {
                    return null;
                }
                ret[pos--] = (byte) val;
                ret[pos--] = (byte) (val >> 8);
            }
        }
        if (isIPv4MappedAddress(ret)) {
            return Arrays.copyOfRange(ret, 12, 16);
        }
        return ret;
    }

    private static boolean isIPv4MappedAddress(byte[] addr) {
        if (addr.length != 16) {
            return false;
        }
        if ((addr[0] == 0x00) && (addr[1] == 0x00)
                && (addr[2] == 0x00) && (addr[3] == 0x00)
                && (addr[4] == 0x00) && (addr[5] == 0x00)
                && (addr[6] == 0x00) && (addr[7] == 0x00)
                && (addr[8] == 0x00) && (addr[9] == 0x00)
                && (addr[10] == (byte) 0xff)
                && (addr[11] == (byte) 0xff)) {
            return true;
        }
        return false;
    }

    // TODO: refactor
    public static long parseLong(final String ip) {
        return ipv4ToLong(ip);
    }

    /**
     * IPv4 to Long.
     * <p>
     * TODO: refactor
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

    // TODO: refactor
    public static int parseInt(final String ipv4) {

        final int position1 = ipv4.indexOf('.');
        final int position2 = ipv4.indexOf('.', position1 + 1);
        final int position3 = ipv4.indexOf('.', position2 + 1);

        return ((Integer.parseInt(ipv4.substring(0, position1)) & 0xFF) << 24)
                | ((Integer.parseInt(ipv4.substring(position1 + 1, position2)) & 0xFF) << 16)
                | ((Integer.parseInt(ipv4.substring(position2 + 1, position3)) & 0xFF) << 8)
                | ((Integer.parseInt(ipv4.substring(position3 + 1)) & 0xFF));
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

    public static long int2long(final int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}
