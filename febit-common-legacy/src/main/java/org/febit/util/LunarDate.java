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
import java.util.Date;

/**
 *
 * @author zqq90
 */
public class LunarDate {

    private static final String[] MOUTHS = {"十", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    private static final char[] DAY_TEN = {'初', '十', '廿'};
    private static final char[] DAY_ONE = {'十', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    private static final String[] ANIMALS = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    private static final char[] GAN = {'甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸'};
    private static final char[] ZHI = {'子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥'};

    private static final long MS_1900_1_31 = -2206425957000L; //new Date(0, 0, 31).getTime();
    /**
     * 农历数据， 1900 - 2049
     */
    private static final long[] LUNAR_DATA = new long[]{
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554,
        0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0,
        0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
        0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550,
        0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0,
        0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263,
        0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
        0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5,
        0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0,
        0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
        0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0,
        0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520,
        0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    };

    private static final int[] DAY_OF_YEAR = {
        384, 354, 355, 383, 354, 355, 384, 354, 355, 384,
        354, 384, 354, 354, 384, 354, 355, 384, 355, 384,
        354, 354, 384, 354, 354, 385, 354, 355, 384, 354,
        383, 354, 355, 384, 355, 354, 384, 354, 384, 354,
        354, 384, 355, 354, 385, 354, 354, 384, 354, 384,
        354, 355, 384, 354, 355, 384, 354, 383, 355, 354,
        384, 355, 354, 384, 355, 353, 384, 355, 384, 354,
        355, 384, 354, 354, 384, 354, 384, 354, 355, 384,
        355, 354, 384, 354, 384, 354, 354, 384, 355, 355,
        384, 354, 354, 383, 355, 384, 354, 355, 384, 354,
        354, 384, 354, 355, 384, 354, 385, 354, 354, 384,
        354, 354, 384, 355, 384, 354, 355, 384, 354, 354,
        384, 354, 355, 384, 354, 384, 354, 354, 384, 355,
        354, 384, 355, 384, 354, 354, 384, 354, 354, 384,
        355, 355, 384, 354, 384, 354, 354, 384, 354, 355
    };

    private final int year;// 农历年  
    private final int month;// 农历月  
    private final int day;// 农历日  
    private final boolean leap;// 农历闰年  

    /**
     * 构造一个表示当前日期的农历日历
     */
    public LunarDate() {
        this(System.currentTimeMillis());
    }

    /**
     * 使用指定日历日期构造一个农历日历
     *
     * @param date
     */
    public LunarDate(Date date) {
        this(date.getTime());
    }

    /**
     * 使用指定日历日期构造一个农历日历
     *
     * @param calendar
     */
    public LunarDate(Calendar calendar) {
        this(calendar.getTimeInMillis());
    }

    /**
     * 使用指定时间戳(毫秒)构造一个农历日历
     *
     * @param millis
     */
    public LunarDate(long millis) {
        // 求出和1900年1月31日相差的天数
        int offset = (int) ((millis - MS_1900_1_31) / 86400000L);

        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            offset -= (daysOfYear = yearDays(iYear));
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
        }

        int leapMonth = leapMonth(iYear); // 闰哪个月,1-12  
        boolean leap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天  
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月  
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(iYear);
            } else {
                daysOfMonth = monthDays(iYear, iMonth);
            }

            offset -= daysOfMonth;
            // 解除闰月  
            if (leap && iMonth == (leapMonth + 1)) {
                leap = false;
            }
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正  
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                --iMonth;
            }
        }
        // offset小于0时，也要校正  
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
        }

        this.month = iMonth;
        this.day = offset + 1;
        this.leap = leap;
        this.year = iYear;
    }

    public String animalsYear() {
        return ANIMALS[(year - 4) % 12];
    }

    public String cyclical() {
        final int num = this.year - 1900 + 36;
        final char[] raw = new char[2];
        raw[0] = GAN[num % 10];
        raw[1] = ZHI[num % 12];
        return new String(raw);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public boolean isLeap() {
        return leap;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder(11)
                .append(this.year)
                .append("年");

        if (this.leap) {
            sb.append("闰");
        }
        sb.append(MOUTHS[month]).append("月");

        int d = this.day;
        if (d == 30) {
            sb.append("三十");
        } else if (d == 20) {
            sb.append("二十");
        } else {
            sb.append(DAY_TEN[d / 10]).append(DAY_ONE[d % 10]);
        }
        return sb.toString();
    }

    /**
     * 该年的总天数
     *
     * @param year
     * @return
     */
    public static int yearDays(int year) {
        return DAY_OF_YEAR[year - 1900];
    }

    protected static int calYearDays(int year) {
        int i, sum = 348;
        final long raw = LUNAR_DATA[year - 1900];
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((raw & i) != 0) {
                sum++;
            }
        }
        return sum + leapDays(year);
    }

    /**
     * 该年闰月的天数
     */
    private static int leapDays(int year) {
        final long raw = LUNAR_DATA[year - 1900];
        if ((int) (raw & 0xf) != 0) {
            return (raw & 0x10000) == 0 ? 29 : 30;
        }
        return 0;
    }

    /**
     * y年闰月 1-12 , 没闰月返回 0
     *
     * @param year
     * @return
     */
    public static int leapMonth(int year) {
        return ((int) LUNAR_DATA[year - 1900]) & 0xf;
    }

    /**
     * y年m月的总天数
     *
     * @param year
     * @param month
     * @return
     */
    public static int monthDays(int year, int month) {
        if ((LUNAR_DATA[year - 1900] & (0x10000 >> month)) == 0) {
            return 29;
        }
        return 30;
    }
}
