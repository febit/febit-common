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
