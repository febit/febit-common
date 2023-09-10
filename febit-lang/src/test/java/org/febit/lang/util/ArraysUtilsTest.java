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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArraysUtilsTest {

    @Test
    public void findIntervalTest() {

        int[] array1 = {2};

        assertEquals(ArraysUtils.findInterval(array1, 1), 0);
        assertEquals(ArraysUtils.findInterval(array1, 2), 0);
        assertEquals(ArraysUtils.findInterval(array1, 3), 1);

        int[] array2 = {2, 6};

        assertEquals(ArraysUtils.findInterval(array2, 1), 0);
        assertEquals(ArraysUtils.findInterval(array2, 2), 0);

        assertEquals(ArraysUtils.findInterval(array2, 5), 1);
        assertEquals(ArraysUtils.findInterval(array2, 6), 1);

        assertEquals(ArraysUtils.findInterval(array2, 7), 2);

        int[] array = {0, 2, 6, 9, 123, 10000};

        assertEquals(ArraysUtils.findInterval(array, -1), 0);
        assertEquals(ArraysUtils.findInterval(array, 0), 0);

        assertEquals(ArraysUtils.findInterval(array, 1), 1);
        assertEquals(ArraysUtils.findInterval(array, 2), 1);

        assertEquals(ArraysUtils.findInterval(array, 5), 2);
        assertEquals(ArraysUtils.findInterval(array, 6), 2);

        assertEquals(ArraysUtils.findInterval(array, 8), 3);
        assertEquals(ArraysUtils.findInterval(array, 9), 3);

        assertEquals(ArraysUtils.findInterval(array, 122), 4);
        assertEquals(ArraysUtils.findInterval(array, 123), 4);

        assertEquals(ArraysUtils.findInterval(array, 124), 5);
        assertEquals(ArraysUtils.findInterval(array, 10000), 5);

        assertEquals(ArraysUtils.findInterval(array, 100000), 6);

    }

    @Test
    void of() {
        assertArrayEquals(new String[]{"1", "2", "3"}, ArraysUtils.of("1", "2", "3"));
    }

    @Test
    void transfer() {
        assertNull(ArraysUtils.transfer(null, String[]::new));
        assertNull(ArraysUtils.transfer(
                (String[]) null, Integer[]::new, Integer::parseInt));

        assertArrayEquals(new String[]{"1", "2", "3"}, ArraysUtils.transfer(Arrays.asList("1", "2", "3"), String[]::new));
        assertArrayEquals(new Integer[]{1, 2, 3}, ArraysUtils.transfer(
                Arrays.asList("1", "2", "3"), Integer[]::new, Integer::parseInt));

        assertArrayEquals(new Integer[]{1, 2, 3}, ArraysUtils.transfer(
                new String[]{"1", "2", "3"}, Integer[]::new, Integer::parseInt));

    }

    @Test
    void collect() {
        assertArrayEquals(new String[]{}, ArraysUtils.collect(null, String[]::new));
        assertArrayEquals(new String[]{}, ArraysUtils.collect(List.of(), String[]::new));
        assertArrayEquals(new String[]{"1", "2", "3"}, ArraysUtils.collect(Arrays.asList("1", "2", "3"), String[]::new));
        assertArrayEquals(new Integer[]{1, 2, 3}, ArraysUtils.collect(
                Arrays.asList("1", "2", "3"), Integer[]::new, Integer::parseInt));

        assertArrayEquals(new Integer[]{}, ArraysUtils.collect(
                (String[]) null, Integer[]::new, Integer::parseInt));
        assertArrayEquals(new Integer[]{}, ArraysUtils.collect(
                new String[]{}, Integer[]::new, Integer::parseInt));
        assertArrayEquals(new Integer[]{1, 2, 3}, ArraysUtils.collect(
                new String[]{"1", "2", "3"}, Integer[]::new, Integer::parseInt));
    }

    @Test
    void get() {
        //noinspection ConstantValue
        assertNull(ArraysUtils.get(null, 0));

        assertEquals("1", ArraysUtils.get(new String[]{"1", "2", "3"}, 0));
        assertEquals("2", ArraysUtils.get(new String[]{"1", "2", "3"}, 1));
        assertEquals("3", ArraysUtils.get(new String[]{"1", "2", "3"}, 2));
        assertNull(ArraysUtils.get(new String[]{"1", "2", "3"}, 3));
        assertNull(ArraysUtils.get(new String[]{"1", "2", "3"}, -1));

        assertEquals("3", ArraysUtils.get(new String[]{"1", "2", "3"}, 2, "default"));
        assertEquals("default", ArraysUtils.get(new String[]{"1", "2", "3"}, 3, "default"));
        assertEquals("default", ArraysUtils.get(new String[]{"1", "2", "3"}, -1, "default"));
    }

    @Test
    void longs() {
        assertArrayEquals(new long[]{}, ArraysUtils.longs(List.of()));
        assertArrayEquals(new long[]{1, 2, 3}, ArraysUtils.longs(List.of(1L, 2L, 3L)));
        assertArrayEquals(new long[]{1, 0, 3}, ArraysUtils.longs(Arrays.asList(1L, null, 3L), 0));
    }

    @Test
    void ints() {
        assertArrayEquals(new int[]{}, ArraysUtils.ints(List.of()));
        assertArrayEquals(new int[]{1, 2, 3}, ArraysUtils.ints(List.of(1, 2, 3)));
        assertArrayEquals(new int[]{1, 0, 3}, ArraysUtils.ints(Arrays.asList(1, null, 3), 0));
    }
}
