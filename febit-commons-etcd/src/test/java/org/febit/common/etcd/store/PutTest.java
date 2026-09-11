/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
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

class PutTest {

    @Test
    void ofIsOverwrite() {
        var p = Put.of("k", "v");
        assertInstanceOf(Put.Overwrite.class, p);
        assertEquals("k", p.key());
        assertEquals("v", p.value());
    }

    @Test
    void ofIfAbsentIsGuardedByAbsence() {
        var p = Put.ofIfAbsent("k", "v");
        assertInstanceOf(Put.IfAbsent.class, p);
        assertEquals("k", p.key());
        assertEquals("v", p.value());
    }

    @Test
    void ofCasCarriesExpectedRevision() {
        var p = Put.ofCas("k", "v", 9L);
        assertInstanceOf(Put.Cas.class, p);
        assertEquals("k", p.key());
        assertEquals("v", p.value());
        assertEquals(9L, ((Put.Cas<?, ?>) p).modRevision());
    }

    @Test
    void ofCasFromRecordDerivesKeyAndRevision() {
        var rec = KVRecord.ofGeneric("k", "v", 11L);
        var p = Put.ofCas(rec);

        assertInstanceOf(Put.Cas.class, p);
        assertEquals("k", p.key());
        assertEquals("v", p.value());
        assertEquals(11L, ((Put.Cas<?, ?>) p).modRevision());
    }
}
