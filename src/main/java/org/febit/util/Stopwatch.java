package org.febit.util;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zqq90
 */
public class Stopwatch implements Closeable, AutoCloseable {

    public static Stopwatch startNew() {
        return new Stopwatch().start();
    }

    private boolean isStarted;
    private long startNanos;
    private long elapsedNanos;

    public Stopwatch() {
    }

    public boolean isRunning() {
        return isStarted;
    }

    public Stopwatch start() {
        if (isStarted) {
            throw new IllegalStateException("Stopwatch is already running.");
        }
        isStarted = true;
        startNanos = System.nanoTime();
        return this;
    }

    public Stopwatch stop() {
        if (!isStarted) {
            throw new IllegalStateException("Stopwatch is already stopped.");
        }
        isStarted = false;
        elapsedNanos += System.nanoTime() - startNanos;
        return this;
    }

    public Stopwatch reset() {
        elapsedNanos = 0;
        isStarted = false;
        return this;
    }

    public Stopwatch restart() {
        return reset().start();
    }

    public long now() {
        return isStarted
                ? System.nanoTime() - startNanos + elapsedNanos
                : elapsedNanos;
    }

    public long now(TimeUnit timeUnit) {
        return timeUnit.convert(now(), TimeUnit.NANOSECONDS);
    }

    public long nowInDays() {
        return now(TimeUnit.DAYS);
    }

    public long nowInHours() {
        return now(TimeUnit.HOURS);
    }

    public long nowInMinutes() {
        return now(TimeUnit.MINUTES);
    }

    public long nowInSeconds() {
        return now(TimeUnit.SECONDS);
    }

    public long nowInMillis() {
        return now(TimeUnit.MILLISECONDS);
    }

    public long nowInMicros() {
        return now(TimeUnit.MICROSECONDS);
    }

    public long nowInNanos() {
        return now();
    }

    @Override
    public String toString() {
        return Long.toString(nowInNanos());
    }

    @Override
    public void close() {
        if (isStarted) {
            stop();
        }
    }
}
