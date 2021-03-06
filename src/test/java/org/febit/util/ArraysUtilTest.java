/**
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
