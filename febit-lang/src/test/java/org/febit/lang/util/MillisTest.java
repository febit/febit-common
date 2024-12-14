package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MillisTest {

    @Test
    void constants() {
        assertEquals(1000L, Millis.SECOND);
        assertEquals(1000L * 60, Millis.MINUTE);
        assertEquals(1000L * 60 * 60, Millis.HOUR);
        assertEquals(1000L * 60 * 60 * 24, Millis.DAY);
        assertEquals(1000L * 60 * 60 * 24 * 7, Millis.WEEK);
    }

    @Test
    void now() {
        long before = System.currentTimeMillis();
        long now = Millis.now();
        long after = System.currentTimeMillis();
        assertTrue(before <= now);
        assertTrue(now <= after);
    }
}
