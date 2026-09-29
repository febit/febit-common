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
import org.tabletest.junit.TableTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArraysUtilsTest {

    @TableTest("""
            scenario              | array                    | value       | expected
            single, hit boundary  | [2]                      | 2           | 0
            single, below         | [2]                      | 1           | 0
            single, min int       | [2]                      | -2147483648 | 0
            single, above         | [2]                      | 3           | 1
            single, max int       | [2]                      | 2147483647  | 1
            two, min int          | [2, 6]                   | -2147483648 | 0
            two, below first      | [2, 6]                   | 1           | 0
            two, hit first        | [2, 6]                   | 2           | 0
            two, between          | [2, 6]                   | 3           | 1
            two, hit second       | [2, 6]                   | 6           | 1
            two, above second     | [2, 6]                   | 7           | 2
            two, max int          | [2, 6]                   | 2147483647  | 2
            three, below          | [10, 20, 30]             | 5           | 0
            three, hit first      | [10, 20, 30]             | 10          | 0
            three, between 1-2    | [10, 20, 30]             | 11          | 1
            three, hit second     | [10, 20, 30]             | 20          | 1
            three, between 2-3    | [10, 20, 30]             | 21          | 2
            three, hit third      | [10, 20, 30]             | 30          | 2
            three, above          | [10, 20, 30]             | 31          | 3
            four, below           | [5, 15, 25, 35]          | 0           | 0
            four, hit first       | [5, 15, 25, 35]          | 5           | 0
            four, between 1-2     | [5, 15, 25, 35]          | 6           | 1
            four, hit second      | [5, 15, 25, 35]          | 15          | 1
            four, between 2-3     | [5, 15, 25, 35]          | 16          | 2
            four, hit third       | [5, 15, 25, 35]          | 25          | 2
            four, between 3-4     | [5, 15, 25, 35]          | 26          | 3
            four, hit fourth      | [5, 15, 25, 35]          | 35          | 3
            four, above           | [5, 15, 25, 35]          | 36          | 4
            large, below          | [0, 2, 6, 9, 123, 10000] | -1          | 0
            large, hit first      | [0, 2, 6, 9, 123, 10000] | 0           | 0
            large, between 1-2    | [0, 2, 6, 9, 123, 10000] | 1           | 1
            large, hit second     | [0, 2, 6, 9, 123, 10000] | 2           | 1
            large, between 2-3    | [0, 2, 6, 9, 123, 10000] | 5           | 2
            large, hit third      | [0, 2, 6, 9, 123, 10000] | 6           | 2
            large, between 3-4    | [0, 2, 6, 9, 123, 10000] | 8           | 3
            large, hit fourth     | [0, 2, 6, 9, 123, 10000] | 9           | 3
            large, between 4-5    | [0, 2, 6, 9, 123, 10000] | 122         | 4
            large, hit fifth      | [0, 2, 6, 9, 123, 10000] | 123         | 4
            large, between 5-6    | [0, 2, 6, 9, 123, 10000] | 124         | 5
            large, hit sixth      | [0, 2, 6, 9, 123, 10000] | 10000       | 5
            large, above          | [0, 2, 6, 9, 123, 10000] | 100000      | 6
            negative, below       | [-100, -50, -10]         | -200        | 0
            negative, hit first   | [-100, -50, -10]         | -100        | 0
            negative, between 1-2 | [-100, -50, -10]         | -99         | 1
            negative, hit second  | [-100, -50, -10]         | -50         | 1
            negative, between 2-3 | [-100, -50, -10]         | -49         | 2
            negative, hit third   | [-100, -50, -10]         | -10         | 2
            negative, above       | [-100, -50, -10]         | 0           | 3
            mixed, below          | [-10, 0, 10]             | -20         | 0
            mixed, hit first      | [-10, 0, 10]             | -10         | 0
            mixed, between 1-2    | [-10, 0, 10]             | -5          | 1
            mixed, hit second     | [-10, 0, 10]             | 0           | 1
            mixed, between 2-3    | [-10, 0, 10]             | 5           | 2
            mixed, hit third      | [-10, 0, 10]             | 10          | 2
            mixed, above          | [-10, 0, 10]             | 20          | 3
            exact, hit 1          | [1, 3, 5, 7, 9]          | 1           | 0
            exact, hit 3          | [1, 3, 5, 7, 9]          | 3           | 1
            exact, hit 5          | [1, 3, 5, 7, 9]          | 5           | 2
            exact, hit 7          | [1, 3, 5, 7, 9]          | 7           | 3
            exact, hit 9          | [1, 3, 5, 7, 9]          | 9           | 4
            between, 2            | [1, 3, 5, 7, 9]          | 2           | 1
            between, 4            | [1, 3, 5, 7, 9]          | 4           | 2
            between, 6            | [1, 3, 5, 7, 9]          | 6           | 3
            between, 8            | [1, 3, 5, 7, 9]          | 8           | 4
            """)
    void findIntervalInt(int[] array, int value, int expected) {
        assertEquals(expected, ArraysUtils.findInterval(array, value));
    }

    @TableTest("""
            scenario              | array                                | value                | expected
            single, hit           | [100]                                | 100                  | 0
            single, below         | [100]                                | 99                   | 0
            single, min long      | [100]                                | -9223372036854775808 | 0
            single, above         | [100]                                | 101                  | 1
            single, max long      | [100]                                | 9223372036854775807  | 1
            two, min long         | [100, 200]                           | -9223372036854775808 | 0
            two, below            | [100, 200]                           | 50                   | 0
            two, hit first        | [100, 200]                           | 100                  | 0
            two, between          | [100, 200]                           | 150                  | 1
            two, hit second       | [100, 200]                           | 200                  | 1
            two, above            | [100, 200]                           | 300                  | 2
            two, max long         | [100, 200]                           | 9223372036854775807  | 2
            three, below          | [100, 200, 300]                      | 50                   | 0
            three, hit first      | [100, 200, 300]                      | 100                  | 0
            three, between 1-2    | [100, 200, 300]                      | 150                  | 1
            three, hit second     | [100, 200, 300]                      | 200                  | 1
            three, between 2-3    | [100, 200, 300]                      | 250                  | 2
            three, hit third      | [100, 200, 300]                      | 300                  | 2
            three, above          | [100, 200, 300]                      | 350                  | 3
            four, below           | [10, 20, 30, 40]                     | 0                    | 0
            four, hit first       | [10, 20, 30, 40]                     | 10                   | 0
            four, between 1-2     | [10, 20, 30, 40]                     | 15                   | 1
            four, hit second      | [10, 20, 30, 40]                     | 20                   | 1
            four, between 2-3     | [10, 20, 30, 40]                     | 25                   | 2
            four, hit third       | [10, 20, 30, 40]                     | 30                   | 2
            four, between 3-4     | [10, 20, 30, 40]                     | 35                   | 3
            four, hit fourth      | [10, 20, 30, 40]                     | 40                   | 3
            four, above           | [10, 20, 30, 40]                     | 50                   | 4
            large, below          | [1000000000, 2000000000, 5000000000] | 0                    | 0
            large, hit first      | [1000000000, 2000000000, 5000000000] | 1000000000           | 0
            large, between 1-2    | [1000000000, 2000000000, 5000000000] | 1500000000           | 1
            large, hit second     | [1000000000, 2000000000, 5000000000] | 2000000000           | 1
            large, between 2-3    | [1000000000, 2000000000, 5000000000] | 3000000000           | 2
            large, hit third      | [1000000000, 2000000000, 5000000000] | 5000000000           | 2
            large, above          | [1000000000, 2000000000, 5000000000] | 10000000000          | 3
            negative, below       | [-500, -100, -50]                    | -1000                | 0
            negative, hit first   | [-500, -100, -50]                    | -500                 | 0
            negative, between 1-2 | [-500, -100, -50]                    | -200                 | 1
            negative, hit second  | [-500, -100, -50]                    | -100                 | 1
            negative, between 2-3 | [-500, -100, -50]                    | -60                  | 2
            negative, hit third   | [-500, -100, -50]                    | -50                  | 2
            negative, above       | [-500, -100, -50]                    | 0                    | 3
            exact, hit 1          | [1, 2, 4, 8, 16]                     | 1                    | 0
            exact, hit 2          | [1, 2, 4, 8, 16]                     | 2                    | 1
            exact, hit 4          | [1, 2, 4, 8, 16]                     | 4                    | 2
            exact, hit 8          | [1, 2, 4, 8, 16]                     | 8                    | 3
            exact, hit 16         | [1, 2, 4, 8, 16]                     | 16                   | 4
            between, 2            | [1, 2, 4, 8, 16]                     | 2                    | 1
            between, 3            | [1, 2, 4, 8, 16]                     | 3                    | 2
            between, 4            | [1, 2, 4, 8, 16]                     | 4                    | 2
            between, 5            | [1, 2, 4, 8, 16]                     | 5                    | 3
            between, 16           | [1, 2, 4, 8, 16]                     | 16                   | 4
            between, 17           | [1, 2, 4, 8, 16]                     | 17                   | 5
            """)
    void findIntervalLong(long[] array, long value, long expected) {
        assertEquals(expected, ArraysUtils.findInterval(array, value));
    }

    @TableTest("""
            input           | expected
            []              | []
            ["a"]           | ["a"]
            ["1", "2", "3"] | ["1", "2", "3"]
            """)
    void of(String[] input, String[] expected) {
        assertArrayEquals(expected, ArraysUtils.of(input));
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

    @TableTest("""
            index | defaultValue | expected
            0     |              | 1
            1     |              | 2
            2     |              | 3
            3     |              |
            -1    |              |
            2     | default      | 3
            3     | default      | default
            -1    | default      | default
            """)
    void get(int index, String defaultValue, String expected) {
        assertEquals(expected, ArraysUtils.get(new String[]{"1", "2", "3"}, index, defaultValue));
    }

    @Test
    void get_nullArray() {
        //noinspection ConstantValue
        assertNull(ArraysUtils.get(null, 0));
    }

    @TableTest("""
            input     | defaultValue | expected
            []        | 0            | []
            [1, 2, 3] | 0            | [1, 2, 3]
            [1, 2, 3] | 9            | [1, 2, 3]
            """)
    void longs(long[] input, long defaultValue, long[] expected) {
        assertArrayEquals(expected, ArraysUtils.longs(Arrays.stream(input).boxed().toList(), defaultValue));
    }

    @Test
    void longs_replacesNullWithDefault() {
        assertArrayEquals(new long[]{1, 0, 3}, ArraysUtils.longs(Arrays.asList(1L, null, 3L), 0));
    }

    @TableTest("""
            input     | defaultValue | expected
            []        | 0            | []
            [1, 2, 3] | 0            | [1, 2, 3]
            [1, 2, 3] | 9            | [1, 2, 3]
            """)
    void ints(int[] input, int defaultValue, int[] expected) {
        assertArrayEquals(expected, ArraysUtils.ints(Arrays.stream(input).boxed().toList(), defaultValue));
    }

    @Test
    void ints_replacesNullWithDefault() {
        assertArrayEquals(new int[]{1, 0, 3}, ArraysUtils.ints(Arrays.asList(1, null, 3), 0));
    }
}
