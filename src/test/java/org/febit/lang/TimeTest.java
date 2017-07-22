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
package org.febit.lang;

import java.util.Calendar;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class TimeTest {

    @Test
    public void test() {
        long millis = System.currentTimeMillis();
        Time time = new Time(millis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        assertEquals(time.year, calendar.get(Calendar.YEAR));
        assertEquals(time.month, calendar.get(Calendar.MONTH) + 1); //[1-12] JANUARY - DECEMBER
        assertEquals(time.day, calendar.get(Calendar.DAY_OF_MONTH)); //[1-31]
        assertEquals(time.hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(time.minute, calendar.get(Calendar.MINUTE));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        assertEquals(time.dayOfWeek, dayOfWeek); //  1 (Monday), ... 6(SATURDAY), 7(Sunday),
    }
}
