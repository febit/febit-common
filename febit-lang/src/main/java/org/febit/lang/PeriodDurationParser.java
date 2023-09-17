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

import jakarta.annotation.Nullable;
import org.febit.lang.util.CharUtils;
import org.febit.lang.util.StringWalker;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PeriodDurationParser {

    private final StringWalker walker;
    private final PeriodDuration.Builder builder = PeriodDuration.builder();

    private int pos = 0;
    @Nullable
    private Long pendingNumber;
    @Nullable
    private Boolean never;

    private PeriodDurationParser(String source) {
        this.walker = new StringWalker(source);
    }

    public static PeriodDuration parse(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return PeriodDuration.NEVER;
        }

        // ISO-8601
        if (raw.charAt(0) == 'P') {
            return PeriodDuration.of(
                    Duration.parse(raw)
            );
        }
        return new PeriodDurationParser(raw).parse0();
    }

    private PeriodDuration parse0() {
        walker.skipBlanks();
        while (!walker.isEnd()) {
            pos = walker.pos();
            var isNumber = CharUtils.isDigit(walker.peek());
            if (isNumber) {
                takeNumber(Long.parseLong(
                        walker.readUntil(CharUtils::isNotDigit)
                ));
            } else {
                takeWord(
                        walker.readUntil(CharUtils::isNotAlpha)
                );
            }
            walker.skipBlanks();
        }
        return pop();
    }

    private PeriodDuration pop() {
        if (Boolean.TRUE.equals(never)) {
            return PeriodDuration.NEVER;
        }
        if (pendingNumber != null) {
            builder.add(pendingNumber, ChronoUnit.SECONDS);
            pendingNumber = null;
        }
        return builder.build();
    }

    private IllegalArgumentException parserException(String msg) {
        throw new IllegalArgumentException(msg + ", pos='" + pos);
    }

    private void takeWord(String word) {
        switch (word.toLowerCase()) {
            case "and":
                // Ignored
                if (pendingNumber != null) {
                    takeUnit(ChronoUnit.SECONDS);
                }
                return;
            case "never":
                takeNever();
                return;
            case "s":
            case "sec":
            case "secs":
            case "second":
            case "seconds":
                takeUnit(ChronoUnit.SECONDS);
                return;
            case "m":
            case "min":
            case "mins":
            case "minute":
            case "minutes":
                takeUnit(ChronoUnit.MINUTES);
                return;
            case "h":
            case "hr":
            case "hrs":
            case "hour":
            case "hours":
                takeUnit(ChronoUnit.HOURS);
                return;
            case "d":
            case "day":
            case "days":
                takeUnit(ChronoUnit.DAYS);
                return;
            case "w":
            case "wk":
            case "wks":
            case "week":
            case "weeks":
                takeUnit(ChronoUnit.WEEKS);
                return;
            case "mos":
            case "mon":
            case "mons":
            case "month":
            case "months":
                takeUnit(ChronoUnit.MONTHS);
                return;
            case "y":
            case "yr":
            case "yrs":
            case "year":
            case "years":
                takeUnit(ChronoUnit.YEARS);
                return;
            case "zero":
                takeNumber(0);
                return;
            case "one":
                takeNumber(1);
                return;
            case "two":
                takeNumber(2);
                return;
            case "three":
                takeNumber(3);
                return;
            case "four":
                takeNumber(4);
                return;
            case "five":
                takeNumber(5);
                return;
            case "six":
                takeNumber(6);
                return;
            case "seven":
                takeNumber(7);
                return;
            case "eight":
                takeNumber(8);
                return;
            case "nine":
                takeNumber(9);
                return;
            case "ten":
                takeNumber(10);
                return;
            case "eleven":
                takeNumber(11);
                return;
            case "twelve":
                takeNumber(12);
                return;
            case "thirteen":
                takeNumber(13);
                return;
            case "fourteen":
                takeNumber(14);
                return;
            case "fifteen":
                takeNumber(15);
                return;
            case "sixteen":
                takeNumber(16);
                return;
            case "seventeen":
                takeNumber(17);
                return;
            case "eighteen":
                takeNumber(18);
                return;
            case "nineteen":
                takeNumber(19);
                return;
            case "twenty":
                takeNumber(20);
                return;
            case "thirty":
                takeNumber(30);
                return;
            case "forty":
                takeNumber(40);
                return;
            case "fifty":
                takeNumber(50);
                return;
            case "sixty":
                takeNumber(60);
                return;
            case "seventy":
                takeNumber(70);
                return;
            case "eighty":
                takeNumber(80);
                return;
            case "ninety":
                takeNumber(90);
                return;
            default:
        }
        throw parserException("Illegal word '" + word + "'");
    }

    private void takeNumber(long number) {
        if (Boolean.TRUE.equals(never)) {
            throw parserException("Number is not allowed after 'never'");
        }
        if (pendingNumber != null) {
            throw parserException("Number is not allowed after number");
        }
        never = false;
        pendingNumber = number;
    }

    private void takeUnit(ChronoUnit unit) {
        if (Boolean.TRUE.equals(never)) {
            throw parserException("Temporal unit is not allowed after 'never'");
        }
        if (pendingNumber == null) {
            throw parserException("Number is required before temporal unit");
        }
        never = false;
        builder.add(pendingNumber, unit);
        pendingNumber = null;
    }

    private void takeNever() {
        if (Boolean.FALSE.equals(never)) {
            throw parserException("'never' is not allowed after number/unit");
        }
        never = true;
    }
}
