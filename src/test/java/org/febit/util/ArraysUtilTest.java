package org.febit.util;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class ArraysUtilTest {

    @Test
    public void findIntervalTest() {

        int[] array1 = {2};

        assertEquals(ArraysUtil.searchInterval(array1, 1), 0);
        assertEquals(ArraysUtil.searchInterval(array1, 2), 0);
        assertEquals(ArraysUtil.searchInterval(array1, 3), 1);

        int[] array2 = {2, 6};

        assertEquals(ArraysUtil.searchInterval(array2, 1), 0);
        assertEquals(ArraysUtil.searchInterval(array2, 2), 0);

        assertEquals(ArraysUtil.searchInterval(array2, 5), 1);
        assertEquals(ArraysUtil.searchInterval(array2, 6), 1);

        assertEquals(ArraysUtil.searchInterval(array2, 7), 2);

        int[] array = {0, 2, 6, 9, 123, 10000};

        assertEquals(ArraysUtil.searchInterval(array, -1), 0);
        assertEquals(ArraysUtil.searchInterval(array, 0), 0);

        assertEquals(ArraysUtil.searchInterval(array, 1), 1);
        assertEquals(ArraysUtil.searchInterval(array, 2), 1);

        assertEquals(ArraysUtil.searchInterval(array, 5), 2);
        assertEquals(ArraysUtil.searchInterval(array, 6), 2);

        assertEquals(ArraysUtil.searchInterval(array, 8), 3);
        assertEquals(ArraysUtil.searchInterval(array, 9), 3);

        assertEquals(ArraysUtil.searchInterval(array, 122), 4);
        assertEquals(ArraysUtil.searchInterval(array, 123), 4);

        assertEquals(ArraysUtil.searchInterval(array, 124), 5);
        assertEquals(ArraysUtil.searchInterval(array, 10000), 5);

        assertEquals(ArraysUtil.searchInterval(array, 100000), 6);

    }
}
