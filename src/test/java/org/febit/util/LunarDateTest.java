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

import java.util.Date;
import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 *
 * @author zqq90
 */
public class LunarDateTest {

    @Test
    public void test() {

        assertEquals(0, LunarDate.leapMonth(2015));
        assertEquals(9, LunarDate.leapMonth(2014));
        assertEquals(8, LunarDate.leapMonth(1995));

        assertEquals(30, LunarDate.monthDays(2014, 12));
        assertEquals(30, LunarDate.monthDays(2013, 12));
        assertEquals(29, LunarDate.monthDays(2012, 12));

        //
        Date date20150218 = new Date(2015 - 1900, 2 - 1, 18);
        LunarDate lunar20141230 = new LunarDate(date20150218);

        Date date20150219 = new Date(2015 - 1900, 2 - 1, 19);
        LunarDate lunar20150101 = new LunarDate(date20150219);

        assertEquals("2014年十二月三十", lunar20141230.toString());
        assertEquals("2015年一月初一", lunar20150101.toString());

        assertEquals("马", lunar20141230.animalsYear());
        assertEquals("羊", lunar20150101.animalsYear());

        assertEquals("甲午", lunar20141230.cyclical());
        assertEquals("乙未", lunar20150101.cyclical());

    }
}
