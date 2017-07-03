package org.febit.util;

import org.febit.util.ip.IpUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
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
}
