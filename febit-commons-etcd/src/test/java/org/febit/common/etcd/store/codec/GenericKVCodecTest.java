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
package org.febit.common.etcd.store.codec;

import org.junit.jupiter.api.Test;

import org.febit.common.etcd.support.TestCodecs;
import org.febit.lang.util.PatternFormatter;

import java.nio.charset.StandardCharsets;

import static org.febit.common.etcd.support.TestSupport.kvOf;
import static org.junit.jupiter.api.Assertions.*;

class GenericKVCodecTest {

    @Test
    void wiresKeyAndValueCodecs() {
        var codec = GenericKVCodec.of(TestCodecs.stringCodec(), TestCodecs.stringCodec());
        assertNotNull(codec.key());
        assertNotNull(codec.value());
    }

    @Test
    void ofJsonifiedWiresPatternKeyAndJsonValue() {
        var formatter = PatternFormatter.<IdKey>builder()
                .text("k/")
                .regex("id", "\\w+")
                .build(IdKey.class);
        var codec = GenericKVCodec.ofJsonified(formatter, IdKey.class);

        assertInstanceOf(PatternCodec.class, codec.key());
        assertInstanceOf(JsonCodec.class, codec.value());

        var encodedKey = codec.key().encode(new IdKey("abc"));
        var encodedValue = codec.value().encode(new IdKey("abc"));
        assertNotNull(encodedKey);
        assertNotNull(encodedValue);

        var rec = codec.decode(kvOf("k/abc", "{\"id\":\"abc\"}", 1L));
        assertNotNull(rec);
        assertEquals(new IdKey("abc"), rec.key());
        assertEquals(new IdKey("abc"), rec.value());
    }

    @Test
    void decodeBuildsRecordFromKeyValue() {
        var codec = TestCodecs.stringKVCodec();
        var kv = kvOf("k", "v", 5L);

        var rec = codec.decode(kv);

        assertNotNull(rec);
        assertEquals("k", rec.key());
        assertEquals("v", rec.value());
        assertEquals(5L, rec.revision());
    }

    @Test
    void decodeReturnsNullWhenKeyValueIsNull() {
        var codec = TestCodecs.stringKVCodec();
        assertNull(codec.decode(null));
    }

    @Test
    void decodeReturnsNullWhenValueUnparseable() {
        var codec = GenericKVCodec.of(TestCodecs.stringCodec(), JsonCodec.of(Sample.class));
        assertNull(codec.decode(kvOf("k", "", 1L)));
    }

    @Test
    void encodeRoundTripsThroughCodecs() {
        var codec = TestCodecs.stringKVCodec();
        var encodedKey = codec.key().encode("k");
        assertEquals("k", new String(encodedKey.getBytes(), StandardCharsets.UTF_8));
    }

    record IdKey(String id) {
    }

    record Sample(String name) {
    }
}
