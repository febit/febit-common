package org.febit.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.stream.Stream;

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

    @Test
    void bad() {
        assertThrows(IllegalArgumentException.class, () -> parse("1 1"));
        assertThrows(IllegalArgumentException.class, () -> parse("s"));
        assertThrows(IllegalArgumentException.class, () -> parse("1s s"));
        assertThrows(IllegalArgumentException.class, () -> parse("abc"));
    }

    @Test
    void bad_never() {
        assertThrows(IllegalArgumentException.class, () -> parse("1 never"));
        assertThrows(IllegalArgumentException.class, () -> parse("1s never"));
        assertThrows(IllegalArgumentException.class, () -> parse("never 1"));
        assertThrows(IllegalArgumentException.class, () -> parse("never s"));
    }

    @Test
    void units() {
        Stream.of(
                "1 years 2 months 3 weeks 4 days 5 hours 6 minutes 7 seconds",
                "1 year 2 month 3 week 4 day 5 hour 6 minute 7 second",
                "1yrs 2mons 3wks 4days 5hrs 6mins 7secs",
                "1yr 2mon 3wk 4day 5hr 6min 7sec",
                "1y 2mos 3w4d5h6m7s"
        ).forEach(raw -> {
            assertEquals(DU_1Y2M3W4D5H6M7S, parse(raw));
        });
    }

    @Test
    void and() {
        assertEquals(PeriodDuration.ofSeconds(1), parse("1s and 0s"));
        assertEquals(PeriodDuration.ofSeconds(5), parse("1s and 0s and 4s"));
        assertEquals(PeriodDuration.ofSeconds(10), parse("1and4and5"));
    }

    @Test
    void iso() {
        assertEquals(Duration.parse("PT1H"), parse("PT1H").toDuration());
        assertEquals(Duration.parse("P2DT1H"), parse("P2DT1H").toDuration());
    }

    @Test
    void never() {
        assertEquals(PeriodDuration.NEVER, parse(null));
        assertEquals(PeriodDuration.NEVER, parse(""));
        assertEquals(PeriodDuration.NEVER, parse("never"));
        assertEquals(PeriodDuration.NEVER, parse("NEVER"));
        assertEquals(PeriodDuration.NEVER, parse("Never"));
        assertEquals(PeriodDuration.NEVER, parse("nEvEr"));
        assertEquals(PeriodDuration.NEVER, parse("  \r \n\tNEvER "));
    }

    @Test
    void blanks() {
        assertEquals(PeriodDuration.ofSeconds(1), parse(" 1 "));
        assertEquals(PeriodDuration.ofSeconds(1), parse("\n  \n \t1\ns \r \n\t"));
    }

    @Test
    void zero() {
        assertEquals(PeriodDuration.ZERO, parse("0"));
        assertEquals(PeriodDuration.ZERO, parse("0s"));
        assertEquals(PeriodDuration.ZERO, parse("0S"));
        assertEquals(PeriodDuration.ZERO, parse("0sec"));
        assertEquals(PeriodDuration.ZERO, parse("0Sec"));
        assertEquals(PeriodDuration.ZERO, parse("0SEC"));
        assertEquals(PeriodDuration.ZERO, parse("0second"));
        assertEquals(PeriodDuration.ZERO, parse("0Second"));
        assertEquals(PeriodDuration.ZERO, parse("0SECOND"));
        assertEquals(PeriodDuration.ZERO, parse("0seconds"));
        assertEquals(PeriodDuration.ZERO, parse("0Seconds"));

        assertEquals(PeriodDuration.ZERO, parse("0m zero s 0d"));
    }

    @Test
    void numbers() {
        var numbers = new String[]{
                "zero",
                "one",
                "two",
                "three",
                "four",
                "five",
                "six",
                "seven",
                "eight",
                "nine",
                "ten",
                "eleven",
                "twelve",
                "thirteen",
                "fourteen",
                "fifteen",
                "sixteen",
                "seventeen",
                "eighteen",
                "nineteen",
                "twenty"
        };
        for (int i = 0; i < numbers.length; i++) {
            assertEquals(PeriodDuration.ofSeconds(i), parse(numbers[i]));
        }

        var tens = new String[]{
                "zero",
                "ten",
                "twenty",
                "thirty",
                "forty",
                "fifty",
                "sixty",
                "seventy",
                "eighty",
                "ninety"
        };
        for (int i = 0; i < tens.length; i++) {
            assertEquals(PeriodDuration.ofSeconds(i * 10), parse(tens[i]));
        }
    }

}
