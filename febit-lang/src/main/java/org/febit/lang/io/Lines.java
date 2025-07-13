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

import lombok.experimental.UtilityClass;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@UtilityClass
public class Lines {

    private static final int BUFFER_SIZE = 1024;

    public static Writer asWriter(Consumer<String> consumer) {
        return LineConsumerWriter.create(consumer);
    }

    public static OutputStream asUtf8OutputStream(Consumer<String> consumer) {
        return asOutputStream(consumer, StandardCharsets.UTF_8);
    }

    public static OutputStream asOutputStream(Consumer<String> consumer, Charset charset) {
        var writer = asWriter(consumer);
        try {
            return WriterOutputStream.builder()
                    .setWriter(writer)
                    .setCharset(charset)
                    .setBufferSize(BUFFER_SIZE)
                    .setWriteImmediately(true)
                    .get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
