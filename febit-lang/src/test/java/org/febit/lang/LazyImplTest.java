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
package org.febit.lang;

import org.febit.lang.func.SerializableSupplier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LazyImplTest {

    @Test
    @SuppressWarnings("unchecked")
    void test() {
        var supplier = mock(SerializableSupplier.class);
        when(supplier.get()).thenReturn("test");

        var lazy = new LazyImpl<String>(supplier);
        verify(supplier, never()).get();

        assertEquals("test", lazy.get());
        assertEquals("test", lazy.get());
        assertEquals("test", lazy.get());
        verify(supplier).get();

        // Reset
        lazy.reset();
        verify(supplier).get();

        assertEquals("test", lazy.get());
        assertEquals("test", lazy.get());
        assertEquals("test", lazy.get());
        verify(supplier, times(2)).get();
    }

    @Test
    @SuppressWarnings("unchecked")
    void ex_npe() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> new LazyImpl<>(null));

        var supplier = mock(SerializableSupplier.class);
        var lazy = new LazyImpl<String>(supplier);

        when(supplier.get()).thenReturn(null);
        assertThrows(NullPointerException.class, lazy::get);

        when(supplier.get()).thenReturn("");
        assertDoesNotThrow(lazy::get);
    }

}
