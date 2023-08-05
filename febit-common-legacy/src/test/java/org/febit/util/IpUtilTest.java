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
package org.febit.util;

import org.febit.util.ip.IpUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.testng.Assert.assertEquals;

/**
 * @author zqq90
 */
public class IpUtilTest {

    @Test
    public void ipv4ToLongTest() {

        assertEquals(IpUtil.ipv4ToLong("0.0.0.0"), 0L);
        assertEquals(IpUtil.ipv4ToLong("255.255.255.255"), 0xFFFFFFFFL);
        assertEquals(IpUtil.ipv4ToLong("25.25.25.25"), 0x19191919L);
        assertEquals(IpUtil.ipv4ToLong("2.2.2.2"), 0x02020202L);

        //bad
        assertEquals(IpUtil.ipv4ToLong(null), -1L);
        assertEquals(IpUtil.ipv4ToLong(""), -1L);
        assertEquals(IpUtil.ipv4ToLong(".0.0.0.0"), -1L);
        assertEquals(IpUtil.ipv4ToLong(".0.0.0.0"), -1L);
        assertEquals(IpUtil.ipv4ToLong("256.255.255.255"), -1L);
        assertEquals(IpUtil.ipv4ToLong(".0.255.255.255"), -1L);

        //crack
        assertEquals(IpUtil.ipv4ToLong("..00000.00"), 0L);
        assertEquals(IpUtil.ipv4ToLong("..1.255"), 0x01FFL);

    }

    @Test
    public void test_ipToBytes() throws UnknownHostException {
        Assert.assertNull(IpUtil.ipToBytes(null));
        Assert.assertNull(IpUtil.ipToBytes(""));
        Assert.assertNull(IpUtil.ipToBytes("localhost"));
        Assert.assertNull(IpUtil.ipToBytes("a.com"));

        Assert.assertEquals(
                IpUtil.ipToBytes("127.0.0.1"),
                ipToBytesByInetAddress("127.0.0.1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("255.255.255.255"),
                ipToBytesByInetAddress("255.255.255.255"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0.0.0.0"),
                ipToBytesByInetAddress("0.0.0.0"));
        Assert.assertEquals(
                IpUtil.ipToBytes("255.0.0.0"),
                ipToBytesByInetAddress("255.0.0.0"));
        Assert.assertEquals(
                IpUtil.ipToBytes("1.2.3.4"),
                ipToBytesByInetAddress("1.2.3.4"));
        Assert.assertEquals(
                IpUtil.ipToBytes("111.112.113.114"),
                ipToBytesByInetAddress("111.112.113.114"));
        Assert.assertEquals(
                IpUtil.ipToBytes("127.127.127.127"),
                ipToBytesByInetAddress("127.127.127.127"));
        Assert.assertEquals(
                IpUtil.ipToBytes("128.128.128.128"),
                ipToBytesByInetAddress("128.128.128.128"));

        Assert.assertEquals(
                IpUtil.ipToBytes("::"),
                ipToBytesByInetAddress("::"));
        Assert.assertEquals(
                IpUtil.ipToBytes("::1"),
                ipToBytesByInetAddress("::1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("::ffff"),
                ipToBytesByInetAddress("::ffff"));
        Assert.assertEquals(
                IpUtil.ipToBytes("::0001"),
                ipToBytesByInetAddress("::0001"));
        Assert.assertEquals(
                IpUtil.ipToBytes("::127.0.0.1"),
                ipToBytesByInetAddress("::127.0.0.1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("::FFFF:127.0.0.1"),
                ipToBytesByInetAddress("::FFFF:127.0.0.1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0::FFFF:127.0.0.1"),
                ipToBytesByInetAddress("0::FFFF:127.0.0.1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("FF01::1"),
                ipToBytesByInetAddress("FF01::1"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0:0:0:0:0:0:0:0"),
                ipToBytesByInetAddress("0:0:0:0:0:0:0:0"));
        Assert.assertEquals(
                IpUtil.ipToBytes("2000::1:2345:6789:abcd"),
                ipToBytesByInetAddress("2000::1:2345:6789:abcd"));
        Assert.assertEquals(
                IpUtil.ipToBytes("2000:0000:0000:0000:0001:2345:6789:abcd"),
                ipToBytesByInetAddress("2000:0000:0000:0000:0001:2345:6789:abcd"));
        Assert.assertEquals(
                IpUtil.ipToBytes("2000:0:0:0:1:2345:6789:abcd"),
                ipToBytesByInetAddress("2000:0:0:0:1:2345:6789:abcd"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0001:0002::0008"),
                ipToBytesByInetAddress("0001:0002::0008"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0001:0002:0003::0006:0007:0008"),
                ipToBytesByInetAddress("0001:0002:0003::0006:0007:0008"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0001:0002:0003:0004::0006:0007:0008"),
                ipToBytesByInetAddress("0001:0002:0003:0004::0006:0007:0008"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0001:0002:0003:0004:0005:0006:0007:0008"),
                ipToBytesByInetAddress("0001:0002:0003:0004:0005:0006:0007:0008"));
        Assert.assertEquals(
                IpUtil.ipToBytes("0001:0002:0003:0004:0005:ffff:111.112.113.114"),
                ipToBytesByInetAddress("0001:0002:0003:0004:0005:ffff:111.112.113.114"));
    }

    private static byte[] ipToBytesByInetAddress(String ip) throws UnknownHostException {
        return InetAddress.getByName(ip).getAddress();
    }

}
