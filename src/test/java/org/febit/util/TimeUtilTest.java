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

    protected static int dayOfYear(int year, int month, int day) {
        return getCalendar(year, month, day).get(Calendar.DAY_OF_YEAR);
    }

    protected static int dayOfWeek(int year, int month, int day) {
        int dayOfWeek = getCalendar(year, month, day).get(Calendar.DAY_OF_WEEK);
        dayOfWeek--;
        return dayOfWeek > 0 ? dayOfWeek : 7;
    }

    protected static Calendar getCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar;
    }
}
