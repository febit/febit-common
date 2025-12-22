package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ObviousNullCheck")
class DefaultsTest {

    @Test
    void nvl() {
        assertEquals("a", Defaults.nvl("a", "b"));
        assertEquals("b", Defaults.nvl(null, "b"));
    }

    @Test
    void collapse() {
        assertEquals("a", Defaults.collapse("a", "b", "c"));
        assertEquals("b", Defaults.collapse(null, "b", "c"));
        assertEquals("c", Defaults.collapse(null, null, "c"));
        assertNull(Defaults.collapse(null, null, null));
        assertNull(Defaults.collapse(null));
    }
}
