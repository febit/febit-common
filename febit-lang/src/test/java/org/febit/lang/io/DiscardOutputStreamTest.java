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
package org.febit.lang.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscardOutputStreamTest {

    @Test
    void discardsWritesAndCloseIsNoop() {
        var out = new DiscardOutputStream();
        assertDoesNotThrow(() -> {
            out.write(1);
            out.write(new byte[]{2, 3, 4});
            out.write(new byte[]{5, 6, 7, 8}, 1, 2);
            out.close();
            out.write(9); // safe to write after close
        });
    }

    @Test
    void instanceIsSharedSingleton() {
        assertNotNull(DiscardOutputStream.INSTANCE);
        assertSame(DiscardOutputStream.INSTANCE, DiscardOutputStream.INSTANCE);
    }
}
