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

import io.etcd.jetcd.ByteSequence;
import org.febit.lang.util.PatternFormatter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatternCodecTest {

    private final PatternFormatter<Acct> formatter = PatternFormatter.<Acct>builder()
            .text("usr/")
            .regex("id", "\\d+")
            .text("/")
            .regex("name", "\\w+")
            .build(Acct.class);
    private final PatternCodec<Acct> codec = PatternCodec.of(formatter);

    @Test
    void encodeDecodeRoundtrip() {
        var bs = codec.encode(new Acct(1L, "alpha"));
        assertThat(CodecUtils.utf8(bs)).isEqualTo("usr/1/alpha");
        assertThat(codec.decode(bs)).isEqualTo(new Acct(1L, "alpha"));
    }

    @Test
    void nonMatchingTextDecodesToNull() {
        assertThat(codec.decode("not-a-match")).isNull();
    }

    @Test
    void nullDecodesToNull() {
        assertThat(codec.decode((ByteSequence) null)).isNull();
        assertThat(codec.decode((String) null)).isNull();
    }

    record Acct(long id, String name) {
    }
}
