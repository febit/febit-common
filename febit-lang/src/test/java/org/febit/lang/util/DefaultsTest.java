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
