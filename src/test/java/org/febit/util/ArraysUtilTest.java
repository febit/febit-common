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

        assertEquals(ArraysUtil.findInterval(array1, 1), 0);
        assertEquals(ArraysUtil.findInterval(array1, 2), 0);
        assertEquals(ArraysUtil.findInterval(array1, 3), 1);

        int[] array2 = {2, 6};

        assertEquals(ArraysUtil.findInterval(array2, 1), 0);
        assertEquals(ArraysUtil.findInterval(array2, 2), 0);

        assertEquals(ArraysUtil.findInterval(array2, 5), 1);
        assertEquals(ArraysUtil.findInterval(array2, 6), 1);

        assertEquals(ArraysUtil.findInterval(array2, 7), 2);

        int[] array = {0, 2, 6, 9, 123, 10000};

        assertEquals(ArraysUtil.findInterval(array, -1), 0);
        assertEquals(ArraysUtil.findInterval(array, 0), 0);

        assertEquals(ArraysUtil.findInterval(array, 1), 1);
        assertEquals(ArraysUtil.findInterval(array, 2), 1);

        assertEquals(ArraysUtil.findInterval(array, 5), 2);
        assertEquals(ArraysUtil.findInterval(array, 6), 2);

        assertEquals(ArraysUtil.findInterval(array, 8), 3);
        assertEquals(ArraysUtil.findInterval(array, 9), 3);

        assertEquals(ArraysUtil.findInterval(array, 122), 4);
        assertEquals(ArraysUtil.findInterval(array, 123), 4);

        assertEquals(ArraysUtil.findInterval(array, 124), 5);
        assertEquals(ArraysUtil.findInterval(array, 10000), 5);

        assertEquals(ArraysUtil.findInterval(array, 100000), 6);

    }
}
