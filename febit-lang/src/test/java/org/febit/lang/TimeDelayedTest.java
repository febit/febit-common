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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeDelayedTest {

    @Test
    @SuppressWarnings("EqualsWithItself")
    void compareTo() {
        var impl0 = Impl.of(0);

        assertEquals(0, impl0.compareTo(impl0));
        assertEquals(0, impl0.compareTo(Impl.of(0)));
        assertEquals(0, impl0.compareTo(Impl2.of(0)));

        assertEquals(-1, impl0.compareTo(Impl.of(1)));
        assertEquals(-1, impl0.compareTo(Impl2.of(1)));
        assertEquals(-1, impl0.compareTo(Impl.of(100)));
        assertEquals(-1, impl0.compareTo(Impl2.of(100)));

        assertEquals(1, impl0.compareTo(Impl.of(-100)));
        assertEquals(1, impl0.compareTo(Impl2.of(-100)));

        var mock = mock(Delayed.class);
        when(mock.getDelay(TimeUnit.NANOSECONDS)).thenReturn(0L);
        assertEquals(-1, impl0.compareTo(mock));
        assertEquals(0, Impl.of(1000).compareTo(mock));
        assertEquals(1, Impl.of(1001).compareTo(mock));
    }

    @TableTest("""
            time | unit         | expected
            1    | MILLISECONDS | -999
            1    | SECONDS      | 0
            2000 | NANOSECONDS  | 1000000000
            2000 | MILLISECONDS | 1000
            2000 | SECONDS      | 1
            """)
    void getDelay(long time, TimeUnit unit, long expected) {
        assertEquals(expected, Impl.of(time).getDelay(unit));
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    static class Impl implements TimeDelayed {
        final long timeInMillis;

        @Override
        public long currentTimeMillis() {
            return 1000;
        }
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    static class Impl2 implements TimeDelayed {
        final long timeInMillis;
    }
}
