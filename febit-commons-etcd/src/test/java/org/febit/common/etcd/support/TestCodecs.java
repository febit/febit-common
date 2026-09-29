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
package org.febit.common.etcd.support;

import io.etcd.jetcd.ByteSequence;

import org.febit.common.etcd.store.codec.Codec;
import org.febit.common.etcd.store.codec.GenericKVCodec;
import org.febit.common.etcd.store.codec.KVCodec;

import java.nio.charset.StandardCharsets;

/**
 * Shared in-test codecs so callers work with typed {@code String} keys/values
 * instead of hand-rolled {@link ByteSequence} literals.
 */
public final class TestCodecs {

    private TestCodecs() {
    }

    public static Codec<String> stringCodec() {
        return new Codec<>() {
            @Override
            public ByteSequence encode(String obj) {
                return ByteSequence.from(obj, StandardCharsets.UTF_8);
            }

            @Override
            public String decode(ByteSequence bytes) {
                return bytes == null ? null : new String(bytes.getBytes(), StandardCharsets.UTF_8);
            }

            @Override
            public String decode(String text) {
                return text;
            }
        };
    }

    public static KVCodec<String, String> stringKVCodec() {
        var c = stringCodec();
        return GenericKVCodec.of(c, c);
    }
}
