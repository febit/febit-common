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
 * @author zqq
 */
public class PriorityTest {

    @Priority.Level(Priority.PRI_HIGH)
    private static class TypeHigh {
    }

    @Priority.High
    private static class TypeHigh2 {
    }

    @Priority.Level(Priority.PRI_NORMAL)
    private static class TypeNormal {
    }

    @Priority.Normal
    private static class TypeNormal2 {
    }

    private static class TypeNormal3 {
    }

    @Priority.Low
    private static class TypeLow {
    }

    @Test
    public void getPriorityTest() {
        assertEquals(Priority.PRI_HIGH, Priority.getPriority(TypeHigh.class));
        assertEquals(Priority.PRI_HIGH, Priority.getPriority(TypeHigh2.class));
        assertEquals(Priority.PRI_NORMAL, Priority.getPriority(TypeNormal.class));
        assertEquals(Priority.PRI_NORMAL, Priority.getPriority(TypeNormal2.class));
        assertEquals(Priority.PRI_NORMAL, Priority.getPriority(TypeNormal3.class));
        assertEquals(Priority.PRI_LOW, Priority.getPriority(TypeLow.class));
    }
}
