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

import java.util.Calendar;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class TimeUtilTest {

    @Test
    public void dayOfYearTest() {
        assertEquals(TimeUtil.dayOfYear(1997, 2, 2), dayOfYear(1997, 2, 2));
        assertEquals(TimeUtil.dayOfYear(1997, 7, 1), dayOfYear(1997, 7, 1));
        assertEquals(TimeUtil.dayOfYear(2000, 1, 1), dayOfYear(2000, 1, 1));
        assertEquals(TimeUtil.dayOfYear(2000, 3, 3), dayOfYear(2000, 3, 3));
        assertEquals(TimeUtil.dayOfYear(2000, 8, 8), dayOfYear(2000, 8, 8));
        assertEquals(TimeUtil.dayOfYear(2008, 8, 8), dayOfYear(2008, 8, 8));
        assertEquals(TimeUtil.dayOfYear(2016, 2, 2), dayOfYear(2016, 2, 2));
        assertEquals(TimeUtil.dayOfYear(2016, 8, 8), dayOfYear(2016, 8, 8));
    }

    @Test
    public void dayOfWeekTest() {
        assertEquals(TimeUtil.dayOfWeek(1997, 2, 2), dayOfWeek(1997, 2, 2));
        assertEquals(TimeUtil.dayOfWeek(1997, 7, 1), dayOfWeek(1997, 7, 1));
        assertEquals(TimeUtil.dayOfWeek(2000, 1, 1), dayOfWeek(2000, 1, 1));
        assertEquals(TimeUtil.dayOfWeek(2000, 3, 3), dayOfWeek(2000, 3, 3));
        assertEquals(TimeUtil.dayOfWeek(2000, 8, 8), dayOfWeek(2000, 8, 8));
        assertEquals(TimeUtil.dayOfWeek(2008, 8, 8), dayOfWeek(2008, 8, 8));
        assertEquals(TimeUtil.dayOfWeek(2016, 2, 2), dayOfWeek(2016, 2, 2));
        assertEquals(TimeUtil.dayOfWeek(2016, 8, 8), dayOfWeek(2016, 8, 8));
    }

    @Test
    public void dayMarkToCalendarTest() {
        assertEquals(TimeUtil.dayMarkToCalendar(19970202), getCalendar(1997, 2, 2));
        assertEquals(TimeUtil.dayMarkToCalendar(19970701), getCalendar(1997, 7, 1));
        assertEquals(TimeUtil.dayMarkToCalendar(20000101), getCalendar(2000, 1, 1));
        assertEquals(TimeUtil.dayMarkToCalendar(20000303), getCalendar(2000, 3, 3));
        assertEquals(TimeUtil.dayMarkToCalendar(20000808), getCalendar(2000, 8, 8));
        assertEquals(TimeUtil.dayMarkToCalendar(20080808), getCalendar(2008, 8, 8));
        assertEquals(TimeUtil.dayMarkToCalendar(20160202), getCalendar(2016, 2, 2));
        assertEquals(TimeUtil.dayMarkToCalendar(20160808), getCalendar(2016, 8, 8));
    }

    @Test
    public void dayMarkTest() {
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(19970202)), 19970202);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(19970701)), 19970701);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20000303)), 20000303);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20000101)), 20000101);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20000808)), 20000808);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20080808)), 20080808);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20160202)), 20160202);
        assertEquals(TimeUtil.getDayMark(TimeUtil.dayMarkToMillis(20160808)), 20160808);
    }

    @Test
    public void addDayMarkTest() {
        assertEquals(TimeUtil.addDayMark(20000808, -1), addDayMark(20000808, -1));
        assertEquals(TimeUtil.addDayMark(20000808, -10), addDayMark(20000808, -10));
        assertEquals(TimeUtil.addDayMark(20000808, -30), addDayMark(20000808, -30));
        assertEquals(TimeUtil.addDayMark(20000808, -100), addDayMark(20000808, -100));
        assertEquals(TimeUtil.addDayMark(20000808, -1000), addDayMark(20000808, -1000));

        assertEquals(TimeUtil.addDayMark(20000808, 0), 20000808);

        assertEquals(TimeUtil.addDayMark(20000808, 1), addDayMark(20000808, 1));
        assertEquals(TimeUtil.addDayMark(20000808, 10), addDayMark(20000808, 10));
        assertEquals(TimeUtil.addDayMark(20000808, 30), addDayMark(20000808, 30));
        assertEquals(TimeUtil.addDayMark(20000808, 100), addDayMark(20000808, 100));
        assertEquals(TimeUtil.addDayMark(20000808, 1000), addDayMark(20000808, 1000));
    }

    protected static int dayOfYear(int year, int month, int day) {
        return getCalendar(year, month, day).get(Calendar.DAY_OF_YEAR);
    }

    protected static int dayOfWeek(int year, int month, int day) {
        int dayOfWeek = getCalendar(year, month, day).get(Calendar.DAY_OF_WEEK);
        dayOfWeek--;
        return dayOfWeek > 0 ? dayOfWeek : 7;
    }

    protected static int addDayMark(int day, int offset) {
        Calendar calendar = TimeUtil.dayMarkToCalendar(day);
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        return TimeUtil.getDayMark(calendar.getTimeInMillis());
    }

    protected static Calendar getCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
