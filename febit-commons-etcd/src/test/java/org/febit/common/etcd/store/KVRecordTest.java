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
package org.febit.common.etcd.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KVRecordTest {

    @Test
    void ofGenericWrapsKeyAndValueAndRevision() {
        var rec = KVRecord.ofGeneric("k", "v", 7L);
        assertEquals("k", rec.key());
        assertEquals("v", rec.value());
        assertEquals(7L, rec.revision());
    }

    @Test
    void withValueKeepsKeyAndRevision() {
        var rec = KVRecord.ofGeneric("k", "v", 7L);
        var next = rec.withValue("v2");

        assertEquals("k", next.key());
        assertEquals("v2", next.value());
        assertEquals(7L, next.revision());
        // original is untouched
        assertEquals("v", rec.value());
    }

    @Test
    void casToBuildsCasPutFromCurrentRevision() {
        var rec = KVRecord.ofGeneric("k", "v", 7L);
        var put = rec.casTo("v2");

        assertInstanceOf(Put.Cas.class, put);
        assertEquals("k", put.key());
        assertEquals("v2", put.value());
        assertEquals(7L, ((Put.Cas<?, ?>) put).modRevision());
    }

    @Test
    void mapAppliesFunctionOverRecord() {
        var rec = KVRecord.ofGeneric("k", "v", 7L);
        assertEquals("k:7", rec.map(r -> r.key() + ":" + r.revision()));
    }
}
