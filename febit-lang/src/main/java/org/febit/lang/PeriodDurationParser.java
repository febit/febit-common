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

import org.febit.lang.util.CharUtils;
import org.febit.lang.util.StringWalker;
import org.jspecify.annotations.Nullable;

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

    @SuppressWarnings({
            "java:S1479" // "switch" statements should not have too many "case" clauses
    })
    private void takeWord(String word) {
        switch (word.toLowerCase()) {
            case "and" -> {
                // Ignored
                if (pendingNumber != null) {
                    takeUnit(ChronoUnit.SECONDS);
                }
            }
            case "never" -> takeNever();
            case "s", "sec", "secs", "second", "seconds" -> takeUnit(ChronoUnit.SECONDS);
            case "m", "min", "mins", "minute", "minutes" -> takeUnit(ChronoUnit.MINUTES);
            case "h", "hr", "hrs", "hour", "hours" -> takeUnit(ChronoUnit.HOURS);
            case "d", "day", "days" -> takeUnit(ChronoUnit.DAYS);
            case "w", "wk", "wks", "week", "weeks" -> takeUnit(ChronoUnit.WEEKS);
            case "mos", "mon", "mons", "month", "months" -> takeUnit(ChronoUnit.MONTHS);
            case "y", "yr", "yrs", "year", "years" -> takeUnit(ChronoUnit.YEARS);
            case "zero" -> takeNumber(0);
            case "one" -> takeNumber(1);
            case "two" -> takeNumber(2);
            case "three" -> takeNumber(3);
            case "four" -> takeNumber(4);
            case "five" -> takeNumber(5);
            case "six" -> takeNumber(6);
            case "seven" -> takeNumber(7);
            case "eight" -> takeNumber(8);
            case "nine" -> takeNumber(9);
            case "ten" -> takeNumber(10);
            case "eleven" -> takeNumber(11);
            case "twelve" -> takeNumber(12);
            case "thirteen" -> takeNumber(13);
            case "fourteen" -> takeNumber(14);
            case "fifteen" -> takeNumber(15);
            case "sixteen" -> takeNumber(16);
            case "seventeen" -> takeNumber(17);
            case "eighteen" -> takeNumber(18);
            case "nineteen" -> takeNumber(19);
            case "twenty" -> takeNumber(20);
            case "thirty" -> takeNumber(30);
            case "forty" -> takeNumber(40);
            case "fifty" -> takeNumber(50);
            case "sixty" -> takeNumber(60);
            case "seventy" -> takeNumber(70);
            case "eighty" -> takeNumber(80);
            case "ninety" -> takeNumber(90);
            default -> throw parserException("Illegal word '" + word + "'");
        }
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
