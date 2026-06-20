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
package org.febit.lang.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallibleTest {

    @Test
    void isFailed_canBeImplementedAsTrue() {
        Fallible f = () -> true;
        assertTrue(f.isFailed());
    }

    @Test
    void isFailed_canBeImplementedAsFalse() {
        Fallible f = () -> false;
        assertFalse(f.isFailed());
    }

    @Test
    void isFailed_returnedFromIResponse() {
        var failed = IResponse.failed("C", "m");
        assertTrue(failed.isFailed());
    }

    @Test
    void isFailed_isFalseForOkResponse() {
        var ok = IResponse.ok("data");
        assertFalse(ok.isFailed());
    }

    @Test
    void interfaceIsAssignableFromResponse() {
        Fallible f = IResponse.ok();
        assertNotNull(f);
    }
}
