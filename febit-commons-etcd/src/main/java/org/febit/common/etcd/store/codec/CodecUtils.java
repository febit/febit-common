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

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

/**
 * Low-level encoding helpers for converting between {@link String} and etcd {@link ByteSequence}.
 */
@UtilityClass
public class CodecUtils {

    public static ByteSequence bytes(String key) {
        return ByteSequence.from(key, StandardCharsets.UTF_8);
    }

    public static String utf8(ByteSequence bytes) {
        return bytes.toString(StandardCharsets.UTF_8);
    }

}
