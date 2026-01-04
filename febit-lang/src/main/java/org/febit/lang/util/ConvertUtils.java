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
package org.febit.lang.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

@UtilityClass
public class ConvertUtils {

    @Nullable
    public static String toString(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static Boolean toBoolean(@Nullable Object raw) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw == null) {
            return false;
        }
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue() == 1D;
        }
        if (raw instanceof String && isTrue((String) raw)) {
            return true;
        }
        var text = raw.toString().trim().toLowerCase();
        return isTrue(text);
    }

    private static Boolean isTrue(String text) {
        return switch (text) {
            case "true", "True", "TRUE",
                 "yes", "Yes", "YES",
                 "y", "Y",
                 "on", "ON", "On",
                 "1" -> true;
            default -> false;
        };
    }

    @Nullable
    public static Long toLong(@Nullable Object obj) {
        return toNumber(obj, Number::longValue);
    }

    @Nullable
    public static Integer toInteger(@Nullable Object obj) {
        return toNumber(obj, Number::intValue);
    }

    @Nullable
    public static Byte toByte(@Nullable Object obj) {
        return toNumber(obj, Number::byteValue);
    }

    @Nullable
    public static Double toDouble(@Nullable Object obj) {
        return toNumber(obj, Number::doubleValue);
    }

    @Nullable
    public static Float toFloat(@Nullable Object obj) {
        return toNumber(obj, Number::floatValue);
    }

    @Nullable
    public static Short toShort(@Nullable Object obj) {
        return toNumber(obj, Number::shortValue);
    }

    @Nullable
    public static <T extends Number> T toNumber(@Nullable Object obj, Function<Number, T> converter) {
        return toNumber(obj, converter, null);
    }

    @Nullable
    public static Number toNumber(@Nullable Object obj) {
        return toNumber(obj, Function.identity());
    }

    /**
     * Convert to number.
     *
     * @throws NumberFormatException – if val is not a valid representation of a Number.
     */
    @Nullable
    public static <T extends Number> T toNumber(
            @Nullable Object raw,
            Function<Number, T> converter,
            @Nullable T defaultValue
    ) {
        if (raw instanceof Number) {
            return converter.apply((Number) raw);
        }
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Character) {
            return converter.apply((int) ((Character) raw));
        }
        var str = raw.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        var decimal = new BigDecimal(str);
        return converter.apply(decimal);
    }

    @Nullable
    public static BigDecimal toBigDecimal(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            }
            var type = obj.getClass();
            if (type == Integer.class
                    || type == Long.class
                    || type == Short.class
                    || type == Byte.class) {
                return BigDecimal.valueOf(((Number) obj).longValue());
            }
            if (obj instanceof Double) {
                return BigDecimal.valueOf(((Number) obj).doubleValue());
            }
            if (obj instanceof BigInteger) {
                return new BigDecimal((BigInteger) obj);
            }
        }
        if (obj instanceof Character) {
            return new BigDecimal((int) ((Character) obj));
        }
        return new BigDecimal(obj.toString().trim());
    }

    @Nullable
    private static <T extends @Nullable Object> T temporal(
            @Nullable Object obj, Function<TemporalAccessor, T> convert, Function<String, T> parser) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof TemporalAccessor) {
            return convert.apply((TemporalAccessor) obj);
        }
        var str = obj.toString();
        return parser.apply(str);
    }

    @Nullable
    private static <T extends @Nullable Object> T utc(
            @Nullable Object obj, Function<ZonedDateTime, T> convert) {
        var time = toUtcZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return convert.apply(time);
    }

    @Nullable
    public static Instant toInstant(@Nullable Object obj) {
        if (obj instanceof Number) {
            return Instant.ofEpochMilli(((Number) obj).longValue());
        }
        return temporal(obj, TimeUtils::instant, TimeUtils::parseInstant);
    }

    @Nullable
    public static Temporal toTemporal(@Nullable Object obj) {
        if (obj instanceof Temporal) {
            return (Temporal) obj;
        }
        return toZonedDateTime(obj);
    }

    @Nullable
    public static Long toMillis(@Nullable Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        var instant = toInstant(obj);
        if (instant == null) {
            return null;
        }
        return instant.toEpochMilli();
    }

    @Nullable
    public static ZoneOffset toZone(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof ZoneOffset) {
            return (ZoneOffset) obj;
        }
        return ZoneOffset.of(obj.toString());
    }

    @Nullable
    public static ZonedDateTime toZonedDateTime(@Nullable Object obj) {
        return temporal(obj, TimeUtils::zonedDateTime, TimeUtils::parseZonedDateTime);
    }

    @Nullable
    public static LocalDateTime toDateTime(@Nullable Object obj) {
        return temporal(obj, TimeUtils::localDateTime, TimeUtils::parseDateTime);
    }

    @Nullable
    public static LocalTime toTime(@Nullable Object obj) {
        return temporal(obj, TimeUtils::localTime, TimeUtils::parseTime);
    }

    @Nullable
    public static LocalDate toDate(@Nullable Object obj) {
        return temporal(obj, TimeUtils::localDate, TimeUtils::parseDate);
    }

    @Nullable
    public static Integer toHour(@Nullable Object obj) {
        return toHour(toTime(obj));
    }

    @Nullable
    public static Integer toDateNumber(@Nullable Object obj) {
        return toDateNumber(toDate(obj));
    }

    @Nullable
    public static ZonedDateTime toUtcZonedDateTime(@Nullable Object obj) {
        var time = toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return time.withZoneSameInstant(ZoneOffset.UTC);
    }

    @Nullable
    public static LocalDateTime toUtcDateTime(@Nullable Object obj) {
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        return utc(obj, ZonedDateTime::toLocalDateTime);
    }

    @Nullable
    public static LocalDate toUtcDate(@Nullable Object obj) {
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        return utc(obj, ZonedDateTime::toLocalDate);
    }

    @Nullable
    public static LocalTime toUtcTime(@Nullable Object obj) {
        if (obj instanceof LocalTime) {
            return (LocalTime) obj;
        }
        var time = toZonedDateTime(obj);
        if (time == null) {
            return null;
        }
        return utc(obj, ZonedDateTime::toLocalTime);
    }

    @Nullable
    public static Integer toUtcHour(@Nullable Object obj) {
        return toHour(toUtcTime(obj));
    }

    @Nullable
    public static Integer toUtcDateNumber(@Nullable Object obj) {
        return toDateNumber(toUtcDate(obj));
    }

    @Nullable
    private static Integer toDateNumber(@Nullable LocalDate time) {
        if (time == null) {
            return null;
        }
        return time.getYear() * 10000
                + time.getMonth().getValue() * 100
                + time.getDayOfMonth();
    }

    @Nullable
    private static Integer toHour(@Nullable LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.getHour();
    }
}
