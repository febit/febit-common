// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq
 */
public class PriorityUtilTest {

    @PriorityUtil.Priority(PriorityUtil.PRI_HIGH)
    private static class TypeHigh {
    }

    @PriorityUtil.High
    private static class TypeHigh2 {
    }

    @PriorityUtil.Priority(PriorityUtil.PRI_NORMAL)
    private static class TypeNormal {
    }

    @PriorityUtil.Normal
    private static class TypeNormal2 {
    }

    private static class TypeNormal3 {
    }

    @PriorityUtil.Low
    private static class TypeLow {
    }

    @Test
    public void getPriorityTest() {
        assertEquals(PriorityUtil.PRI_HIGH, PriorityUtil.getPriority(TypeHigh.class));
        assertEquals(PriorityUtil.PRI_HIGH, PriorityUtil.getPriority(TypeHigh2.class));
        assertEquals(PriorityUtil.PRI_NORMAL, PriorityUtil.getPriority(TypeNormal.class));
        assertEquals(PriorityUtil.PRI_NORMAL, PriorityUtil.getPriority(TypeNormal2.class));
        assertEquals(PriorityUtil.PRI_NORMAL, PriorityUtil.getPriority(TypeNormal3.class));
        assertEquals(PriorityUtil.PRI_LOW, PriorityUtil.getPriority(TypeLow.class));
    }
}
