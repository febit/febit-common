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

import static org.assertj.core.api.Assertions.assertThat;

class CodecUtilsTest {

    @Test
    void bytesThenUtf8Roundtrip() {
        assertThat(CodecUtils.utf8(CodecUtils.bytes("hello/world"))).isEqualTo("hello/world");
    }

    @Test
    void preservesUnicode() {
        var s = "项目-环境_🔐";
        assertThat(CodecUtils.utf8(CodecUtils.bytes(s))).isEqualTo(s);
    }

    @Test
    void emptyString() {
        assertThat(CodecUtils.utf8(CodecUtils.bytes(""))).isEmpty();
    }
}
