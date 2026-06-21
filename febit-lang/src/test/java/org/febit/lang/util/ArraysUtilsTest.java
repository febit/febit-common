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
    void findIntervalInt_singleElement() {
        int[] array = {2};
        assertEquals(0, ArraysUtils.findInterval(array, 2));
        assertEquals(0, ArraysUtils.findInterval(array, 1));
        assertEquals(0, ArraysUtils.findInterval(array, Integer.MIN_VALUE));
        assertEquals(1, ArraysUtils.findInterval(array, 3));
        assertEquals(1, ArraysUtils.findInterval(array, Integer.MAX_VALUE));
    }

    @Test
    void findIntervalInt_twoElements() {
        int[] array = {2, 6};
        assertEquals(0, ArraysUtils.findInterval(array, Integer.MIN_VALUE));
        assertEquals(0, ArraysUtils.findInterval(array, 1));
        assertEquals(0, ArraysUtils.findInterval(array, 2));
        assertEquals(1, ArraysUtils.findInterval(array, 3));
        assertEquals(1, ArraysUtils.findInterval(array, 6));
        assertEquals(2, ArraysUtils.findInterval(array, 7));
        assertEquals(2, ArraysUtils.findInterval(array, Integer.MAX_VALUE));
    }

    @Test
    void findIntervalInt_threeElements() {
        int[] array = {10, 20, 30};
        assertEquals(0, ArraysUtils.findInterval(array, 5));
        assertEquals(0, ArraysUtils.findInterval(array, 10));
        assertEquals(1, ArraysUtils.findInterval(array, 11));
        assertEquals(1, ArraysUtils.findInterval(array, 20));
        assertEquals(2, ArraysUtils.findInterval(array, 21));
        assertEquals(2, ArraysUtils.findInterval(array, 30));
        assertEquals(3, ArraysUtils.findInterval(array, 31));
    }

    @Test
    void findIntervalInt_fourElements() {
        int[] array = {5, 15, 25, 35};
        assertEquals(0, ArraysUtils.findInterval(array, 0));
        assertEquals(0, ArraysUtils.findInterval(array, 5));
        assertEquals(1, ArraysUtils.findInterval(array, 6));
        assertEquals(1, ArraysUtils.findInterval(array, 15));
        assertEquals(2, ArraysUtils.findInterval(array, 16));
        assertEquals(2, ArraysUtils.findInterval(array, 25));
        assertEquals(3, ArraysUtils.findInterval(array, 26));
        assertEquals(3, ArraysUtils.findInterval(array, 35));
        assertEquals(4, ArraysUtils.findInterval(array, 36));
    }

    @Test
    void findIntervalInt_largeArray() {
        int[] array = {0, 2, 6, 9, 123, 10000};
        assertEquals(0, ArraysUtils.findInterval(array, -1));
        assertEquals(0, ArraysUtils.findInterval(array, 0));
        assertEquals(1, ArraysUtils.findInterval(array, 1));
        assertEquals(1, ArraysUtils.findInterval(array, 2));
        assertEquals(2, ArraysUtils.findInterval(array, 5));
        assertEquals(2, ArraysUtils.findInterval(array, 6));
        assertEquals(3, ArraysUtils.findInterval(array, 8));
        assertEquals(3, ArraysUtils.findInterval(array, 9));
        assertEquals(4, ArraysUtils.findInterval(array, 122));
        assertEquals(4, ArraysUtils.findInterval(array, 123));
        assertEquals(5, ArraysUtils.findInterval(array, 124));
        assertEquals(5, ArraysUtils.findInterval(array, 10000));
        assertEquals(6, ArraysUtils.findInterval(array, 100000));
    }

    @Test
    void findIntervalInt_negativeIntervals() {
        int[] array = {-100, -50, -10};
        assertEquals(0, ArraysUtils.findInterval(array, -200));
        assertEquals(0, ArraysUtils.findInterval(array, -100));
        assertEquals(1, ArraysUtils.findInterval(array, -99));
        assertEquals(1, ArraysUtils.findInterval(array, -50));
        assertEquals(2, ArraysUtils.findInterval(array, -49));
        assertEquals(2, ArraysUtils.findInterval(array, -10));
        assertEquals(3, ArraysUtils.findInterval(array, 0));
    }

    @Test
    void findIntervalInt_mixedSignIntervals() {
        int[] array = {-10, 0, 10};
        assertEquals(0, ArraysUtils.findInterval(array, -20));
        assertEquals(0, ArraysUtils.findInterval(array, -10));
        assertEquals(1, ArraysUtils.findInterval(array, -5));
        assertEquals(1, ArraysUtils.findInterval(array, 0));
        assertEquals(2, ArraysUtils.findInterval(array, 5));
        assertEquals(2, ArraysUtils.findInterval(array, 10));
        assertEquals(3, ArraysUtils.findInterval(array, 20));
    }

    @Test
    void findIntervalInt_exactBoundaryHits() {
        int[] array = {1, 3, 5, 7, 9};
        // 每个边界值恰好命中
        assertEquals(0, ArraysUtils.findInterval(array, 1));
        assertEquals(1, ArraysUtils.findInterval(array, 3));
        assertEquals(2, ArraysUtils.findInterval(array, 5));
        assertEquals(3, ArraysUtils.findInterval(array, 7));
        assertEquals(4, ArraysUtils.findInterval(array, 9));
    }

    @Test
    void findIntervalInt_betweenBoundaries() {
        int[] array = {1, 3, 5, 7, 9};
        // 每个区间内的值
        assertEquals(1, ArraysUtils.findInterval(array, 2));
        assertEquals(2, ArraysUtils.findInterval(array, 4));
        assertEquals(3, ArraysUtils.findInterval(array, 6));
        assertEquals(4, ArraysUtils.findInterval(array, 8));
    }

    @Test
    void findIntervalLong_singleElement() {
        long[] array = {100L};
        assertEquals(0, ArraysUtils.findInterval(array, 100L));
        assertEquals(0, ArraysUtils.findInterval(array, 99L));
        assertEquals(0, ArraysUtils.findInterval(array, Long.MIN_VALUE));
        assertEquals(1, ArraysUtils.findInterval(array, 101L));
        assertEquals(1, ArraysUtils.findInterval(array, Long.MAX_VALUE));
    }

    @Test
    void findIntervalLong_twoElements() {
        long[] array = {100L, 200L};
        assertEquals(0, ArraysUtils.findInterval(array, Long.MIN_VALUE));
        assertEquals(0, ArraysUtils.findInterval(array, 50L));
        assertEquals(0, ArraysUtils.findInterval(array, 100L));
        assertEquals(1, ArraysUtils.findInterval(array, 150L));
        assertEquals(1, ArraysUtils.findInterval(array, 200L));
        assertEquals(2, ArraysUtils.findInterval(array, 300L));
        assertEquals(2, ArraysUtils.findInterval(array, Long.MAX_VALUE));
    }

    @Test
    void findIntervalLong_threeElements() {
        long[] array = {100L, 200L, 300L};
        assertEquals(0, ArraysUtils.findInterval(array, 50L));
        assertEquals(0, ArraysUtils.findInterval(array, 100L));
        assertEquals(1, ArraysUtils.findInterval(array, 150L));
        assertEquals(1, ArraysUtils.findInterval(array, 200L));
        assertEquals(2, ArraysUtils.findInterval(array, 250L));
        assertEquals(2, ArraysUtils.findInterval(array, 300L));
        assertEquals(3, ArraysUtils.findInterval(array, 350L));
    }

    @Test
    void findIntervalLong_fourElements() {
        long[] array = {10L, 20L, 30L, 40L};
        assertEquals(0, ArraysUtils.findInterval(array, 0L));
        assertEquals(0, ArraysUtils.findInterval(array, 10L));
        assertEquals(1, ArraysUtils.findInterval(array, 15L));
        assertEquals(1, ArraysUtils.findInterval(array, 20L));
        assertEquals(2, ArraysUtils.findInterval(array, 25L));
        assertEquals(2, ArraysUtils.findInterval(array, 30L));
        assertEquals(3, ArraysUtils.findInterval(array, 35L));
        assertEquals(3, ArraysUtils.findInterval(array, 40L));
        assertEquals(4, ArraysUtils.findInterval(array, 50L));
    }

    @Test
    void findIntervalLong_largeValues() {
        long[] array = {1_000_000_000L, 2_000_000_000L, 5_000_000_000L};
        assertEquals(0, ArraysUtils.findInterval(array, 0L));
        assertEquals(0, ArraysUtils.findInterval(array, 1_000_000_000L));
        assertEquals(1, ArraysUtils.findInterval(array, 1_500_000_000L));
        assertEquals(1, ArraysUtils.findInterval(array, 2_000_000_000L));
        assertEquals(2, ArraysUtils.findInterval(array, 3_000_000_000L));
        assertEquals(2, ArraysUtils.findInterval(array, 5_000_000_000L));
        assertEquals(3, ArraysUtils.findInterval(array, 10_000_000_000L));
    }

    @Test
    void findIntervalLong_negativeIntervals() {
        long[] array = {-500L, -100L, -50L};
        assertEquals(0, ArraysUtils.findInterval(array, -1000L));
        assertEquals(0, ArraysUtils.findInterval(array, -500L));
        assertEquals(1, ArraysUtils.findInterval(array, -200L));
        assertEquals(1, ArraysUtils.findInterval(array, -100L));
        assertEquals(2, ArraysUtils.findInterval(array, -60L));
        assertEquals(2, ArraysUtils.findInterval(array, -50L));
        assertEquals(3, ArraysUtils.findInterval(array, 0L));
    }

    @Test
    void findIntervalLong_exactBoundaryHits() {
        long[] array = {1L, 2L, 4L, 8L, 16L};
        assertEquals(0, ArraysUtils.findInterval(array, 1L));
        assertEquals(1, ArraysUtils.findInterval(array, 2L));
        assertEquals(2, ArraysUtils.findInterval(array, 4L));
        assertEquals(3, ArraysUtils.findInterval(array, 8L));
        assertEquals(4, ArraysUtils.findInterval(array, 16L));
    }

    @Test
    void findIntervalLong_betweenBoundaries() {
        long[] array = {1L, 2L, 4L, 8L, 16L};
        assertEquals(1, ArraysUtils.findInterval(array, 2L));
        assertEquals(2, ArraysUtils.findInterval(array, 3L));
        assertEquals(2, ArraysUtils.findInterval(array, 4L));
        assertEquals(3, ArraysUtils.findInterval(array, 5L));
        assertEquals(4, ArraysUtils.findInterval(array, 16L));
        assertEquals(5, ArraysUtils.findInterval(array, 17L));
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
