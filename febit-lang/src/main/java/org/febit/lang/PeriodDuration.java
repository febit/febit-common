package org.febit.lang;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class PeriodDuration implements TemporalAmount {

    private static final List<TemporalUnit> SUPPORTED_UNITS = List.of(MONTHS, SECONDS);

    public static final String NEVER_RAW = "never";

    public static final PeriodDuration ZERO = of("zero", 0, 0);
    public static final PeriodDuration NEVER = of(NEVER_RAW, 0, 0);

    @JsonValue
    @Nonnull
    @EqualsAndHashCode.Exclude
    private final String raw;

    private final int months;
    private final long seconds;

    public static PeriodDuration of(Duration du) {
        return ofSeconds(du.getSeconds());
    }

    public static PeriodDuration.Builder builder() {
        return new Builder();
    }

    public static PeriodDuration ofSeconds(long seconds) {
        if (seconds == 0) {
            return ZERO;
        }
        return of(String.valueOf(seconds), 0, seconds);
    }

    public static PeriodDuration ofSeconds(Number seconds) {
        return ofSeconds(seconds.longValue());
    }

    public static PeriodDuration parse(String raw) {
        return PeriodDurationParser.parse(raw);
    }

    public boolean isZero() {
        return months == 0 && seconds == 0;
    }

    public boolean isNever() {
        return NEVER_RAW.equalsIgnoreCase(raw);
    }

    public long toSeconds() {
        return seconds + months * MONTHS.getDuration().getSeconds();
    }

    public Duration toDuration() {
        return Duration.ofSeconds(toSeconds());
    }

    private void validateChronology(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal);
        var chronology = temporal.query(TemporalQueries.chronology());
        if (chronology != null && !IsoChronology.INSTANCE.equals(chronology)) {
            throw new DateTimeException("Chronology mismatch, expected: ISO, actual: " + chronology.getId());
        }
    }

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.MONTHS) {
            return getMonths();
        }
        if (unit == SECONDS) {
            return getSeconds();
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        validateChronology(temporal);
        if (months != 0) {
            temporal = temporal.plus(months, MONTHS);
        }
        if (seconds != 0) {
            temporal = temporal.plus(seconds, SECONDS);
        }
        return temporal;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        validateChronology(temporal);
        if (months != 0) {
            temporal = temporal.minus(months, MONTHS);
        }
        if (seconds != 0) {
            temporal = temporal.minus(seconds, SECONDS);
        }
        return temporal;
    }

    @Override
    public String toString() {
        return months + "months" + seconds + "seconds";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private int months = 0;
        private long seconds = 0L;

        public PeriodDuration build() {
            return PeriodDuration.of(months + "months" + seconds + "seconds", months, seconds);
        }

        public Builder minus(long num, ChronoUnit unit) {
            return add(-num, unit);
        }

        public Builder add(long num, ChronoUnit unit) {
            switch (unit) {
                case SECONDS:
                    seconds += num;
                    break;
                case MINUTES:
                case HOURS:
                case DAYS:
                case WEEKS:
                    seconds += num * unit.getDuration().getSeconds();
                    break;
                case MONTHS:
                    months += (int) num;
                    break;
                case YEARS:
                    months += (int) (num * 12);
                    break;
                case NANOS:
                case MICROS:
                case MILLIS:
                case HALF_DAYS:
                case DECADES:
                case CENTURIES:
                case MILLENNIA:
                case ERAS:
                case FOREVER:
                    throw new IllegalStateException("Unsupported temporal unit: " + unit);
            }
            return this;
        }
    }

}
