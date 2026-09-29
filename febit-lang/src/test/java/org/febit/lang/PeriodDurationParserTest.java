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
package org.febit.lang;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.time.Duration;

import static org.febit.lang.PeriodDuration.parse;
import static org.junit.jupiter.api.Assertions.*;

class PeriodDurationParserTest {

    static final PeriodDuration DU_1Y2M3W4D5H6M7S = parse(
            "1 years 2 months 3 weeks 4 days 5 hours 6 minutes 7 seconds");

    @Test
    void du() {
        assertEquals(12 + 2, DU_1Y2M3W4D5H6M7S.getMonths());
        assertEquals(Duration.ZERO.plusDays(3 * 7)
                        .plusDays(4)
                        .plusHours(5)
                        .plusMinutes(6)
                        .plusSeconds(7).getSeconds(),
                DU_1Y2M3W4D5H6M7S.getSeconds()
        );
    }

    @TableTest("""
            input

            ""
            never
            NEVER
            Never
            nEvEr
            """)
    void never(String input) {
        assertEquals(PeriodDuration.NEVER, parse(input));
    }

    @Test
    void never_whitespaceTrim() {
        // Control chars are not representable in a table cell, so kept as a dedicated case.
        assertEquals(PeriodDuration.NEVER, parse("  \r \n\tNEvER "));
    }

    @TableTest("""
            input
            1 1
            s
            1s s
            abc
            1 never
            1s never
            never 1
            never s
            """)
    void bad(String input) {
        assertThrows(IllegalArgumentException.class, () -> parse(input));
    }

    @TableTest("""
            input
            1 years 2 months 3 weeks 4 days 5 hours 6 minutes 7 seconds
            1 year 2 month 3 week 4 day 5 hour 6 minute 7 second
            1yrs 2mons 3wks 4days 5hrs 6mins 7secs
            1yr 2mon 3wk 4day 5hr 6min 7sec
            1y 2mos 3w4d5h6m7s
            """)
    void units(String input) {
        assertEquals(DU_1Y2M3W4D5H6M7S, parse(input));
    }

    @TableTest("""
            input            | seconds
            1s and 0s        | 1
            1s and 0s and 4s | 5
            1and4and5        | 10
            """)
    void and(String input, int seconds) {
        assertEquals(PeriodDuration.ofSeconds(seconds), parse(input));
    }

    @TableTest("""
            input  | expected
            PT1H   | PT1H
            P2DT1H | P2DT1H
            """)
    void iso(String input, String expected) {
        assertEquals(Duration.parse(expected), parse(input).toDuration());
    }

    @TableTest("""
            input
            0
            0s
            0S
            0sec
            0Sec
            0SEC
            0second
            0Second
            0SECOND
            0seconds
            0Seconds
            0m zero s 0d
            """)
    void zero(String input) {
        assertEquals(PeriodDuration.ZERO, parse(input));
    }

    @TableTest("""
            word      | seconds
            zero      | 0
            one       | 1
            two       | 2
            three     | 3
            four      | 4
            five      | 5
            six       | 6
            seven     | 7
            eight     | 8
            nine      | 9
            ten       | 10
            eleven    | 11
            twelve    | 12
            thirteen  | 13
            fourteen  | 14
            fifteen   | 15
            sixteen   | 16
            seventeen | 17
            eighteen  | 18
            nineteen  | 19
            twenty    | 20
            """)
    void numbers(String word, int seconds) {
        assertEquals(PeriodDuration.ofSeconds(seconds), parse(word));
    }

    @TableTest("""
            word    | seconds
            zero    | 0
            ten     | 10
            twenty  | 20
            thirty  | 30
            forty   | 40
            fifty   | 50
            sixty   | 60
            seventy | 70
            eighty  | 80
            ninety  | 90
            """)
    void tens(String word, int seconds) {
        assertEquals(PeriodDuration.ofSeconds(seconds), parse(word));
    }

    @Test
    void blanks() {
        // Real control chars are not representable in a table cell, so kept as dedicated cases.
        assertEquals(PeriodDuration.ofSeconds(1), parse(" 1 "));
        assertEquals(PeriodDuration.ofSeconds(1), parse("\n  \n \t1\ns \r \n\t"));
    }
}
