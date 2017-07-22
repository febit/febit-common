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
import java.util.TimeZone;
import org.febit.lang.Time;

/**
 *
 * @author zqq90
 */
public class TimeUtil {

    public static final long MILLIS_IN_MINUTE = 1000L * 60;
    public static final long MILLIS_IN_HOUR = 1000L * 60 * 60;
    public static final long MILLIS_IN_DAY = 1000L * 60 * 60 * 24;

    private final static int[] DAYS_OF_MONTH = new int[]{
        0,
        31, // 1
        28, // 2
        31, // 3
        30, // 4
        31, // 5
        30, // 6
        31, // 7
        31, // 8
        30, // 9
        31, // 10
        30, // 11
        31 // 12
    };

    private final static int[] DAYS_OF_YEAR_PER_MONTH = new int[]{
        0,
        31, // 1
        59, // 2
        90, // 3
        120, // 4
        151, // 5
        181, // 6
        212, // 7
        243, // 8
        273, // 9
        304, // 10
        334, // 11
        365, // 12
    };

    private static final int[] CONSTELLATION_DATES = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
    private static final String[] CONSTELLATIONS = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

    public static int getMonthLength(final int year, final int month) {
        if (month == 2 && isLeapYear(year)) {
            return 29;
        }
        return DAYS_OF_MONTH[month];
    }

    public static int getMonthLengthOfCommonYear(final int month) {
        return DAYS_OF_MONTH[month];
    }

    public static boolean isLeapYear(final int year) {
        return ((year % 4) == 0) // must be divisible by 4...
                && ((year < 1582) // and either before reform year...
                || ((year % 100) != 0) // or not a century...
                || ((year % 400) == 0)); // or a multiple of 400...
    }

    /**
     * Day of week. Mon Tue ... Sun : 1 2 ... 7
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int dayOfWeek(int year, int month, int day) {
        if (month == 1 || month == 2) {
            month += 12;
            year--;
        }
        return (day + 2 * month + 3 * (month + 1) / 5 + year + year / 4 - year / 100 + year / 400) % 7 + 1;
    }

    public static int dayOfWeek(int dayMark) {
        return dayOfWeek(getYearOfDayMark(dayMark), getMonthOfDayMark(dayMark), getDayOfDayMark(dayMark));
    }

    /**
     * Day of year.
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int dayOfYear(int year, int month, int day) {
        return DAYS_OF_YEAR_PER_MONTH[month - 1]
                + day
                + (month >= 3 && isLeapYear(year) ? 1 : 0);
    }

    public static int dayOfYear(int dayMark) {
        return dayOfYear(getYearOfDayMark(dayMark), getMonthOfDayMark(dayMark), getDayOfDayMark(dayMark));
    }

    public static int offsetOfDayMarks(int daymark, int daymark2) {
        int offset = daymark - daymark2;
        if (offset >= -30 && offset <= 30) {
            //same month
            return offset;
        }
        if (getYearOfDayMark(daymark) == getYearOfDayMark(daymark2)) {
            //same year
            return dayOfYear(daymark) - dayOfYear(daymark2);
        }
        return (int) ((dayMarkToMillis(daymark)
                - dayMarkToMillis(daymark2))
                / MILLIS_IN_DAY);
    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getRawOffset()) / MILLIS_IN_DAY;
    }

    public static long toDayMillis(long millis) {
        long offset = TimeZone.getDefault().getRawOffset();
        return ((millis + offset) / MILLIS_IN_DAY) * MILLIS_IN_DAY - offset;
    }

    public static long toMonthMillis(long millis) {
        Time time = new Time(millis);
        return toDayMillis(millis) - MILLIS_IN_DAY * (time.day - 1);
    }

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY
                && interval > -1L * MILLIS_IN_DAY
                && toDay(ms1) == toDay(ms2);
    }

    public static boolean isSameMinuteOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_MINUTE
                && interval > -1L * MILLIS_IN_MINUTE
                && (ms1 / MILLIS_IN_MINUTE == ms2 / MILLIS_IN_MINUTE);
    }

    public static boolean isSameSecondOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < 1000L
                && interval > -1000L
                && (ms1 / 1000L == ms2 / 1000L);
    }

    public static String getConstellation(int index) {
        return CONSTELLATIONS[index];
    }

    public static int getConstellationIndex(int month, int day) {
        return day < CONSTELLATION_DATES[month - 1] ? month - 1 : month;
    }

    public static String getConstellation(int month, int day) {
        return getConstellation(getConstellationIndex(month, day));
    }

    private static MonthMarkEntry monthEntry;
    private static MonthMillisEntry monthMillisEntry;

    public static long getMonthMillis() {
        return getMonthMillis(System.currentTimeMillis());
    }

    public static int getDayMark() {
        return getDayMark(System.currentTimeMillis());
    }

    public static int getDayMark(final long ms) {
        Time time = new Time(ms);
        return time.year * 10000
                + time.month * 100
                + time.day;
    }

    public static long dayMarkToMillis(int day) {
        return dayMarkToCalendar(day).getTimeInMillis();
    }

    public static int getYearOfDayMark(int day) {
        return day / 10000;
    }

    public static int getMonthOfDayMark(int day) {
        return (day % 10000) / 100;
    }

    public static int getDayOfDayMark(int day) {
        return day % 100;
    }

    public static int getDayMark(Calendar calendar) {
        return calendar.get(Calendar.YEAR) * 10000
                + (calendar.get(Calendar.MONTH) + 1) * 100
                + calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Calendar dayMarkToCalendar(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYearOfDayMark(day), getMonthOfDayMark(day) - 1, getDayOfDayMark(day), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static int addDayMark(int day, int offset) {
        return getDayMark(dayMarkToMillis(day) + offset * MILLIS_IN_DAY);
    }

    public static int getMonthMark() {
        return getMonthMark(System.currentTimeMillis());
    }

    public static int addMonthMark(int month, int add) {
        int m = month % 100 + add;
        if (m > 0 && m <= 12) {
            return month + add;
        }
        int y = month / 100;
        int totalMonth = y * 12 + m - 1;
        y = totalMonth / 12;
        m = totalMonth - y * 12 + 1;
        return y * 100 + m;
    }

    public static int getMonthMark(final long ms) {
        MonthMarkEntry cached = monthEntry;
        if (cached != null) {
            long interval = ms - cached.time;
            if (interval >= 0 && interval < MILLIS_IN_DAY) {
                return cached.month;
            }
        }
        Time time = new Time(ms);
        int month = time.year * 100 + time.month;
        monthEntry = new MonthMarkEntry(ms, month);
        return month;
    }

    public static long getMonthMillis(final long ms) {
        MonthMillisEntry cached = monthMillisEntry;
        if (cached != null) {
            long interval = ms - cached.time;
            if (interval >= 0 && interval < MILLIS_IN_DAY) {
                return cached.month;
            }
        }
        long month = toMonthMillis(ms);
        monthMillisEntry = new MonthMillisEntry(ms, month);
        return month;
    }

    private static class MonthMarkEntry {

        protected final long time;
        protected final int month;

        protected MonthMarkEntry(long time, int month) {
            this.time = toDayMillis(time);
            this.month = month;
        }
    }

    private static class MonthMillisEntry {

        protected final long time;
        protected final long month;

        protected MonthMillisEntry(long time, long month) {
            this.time = toDayMillis(time);
            this.month = month;
        }
    }
}
