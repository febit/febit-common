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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class LinesTest {

    @Test
    void asWriter() throws IOException {
        var consumer = new ArrayList<String>();
        try (var writer = Lines.asWriter(consumer::add)) {
            writer.write("Hello\nWorld");
        }
        assertEquals(List.of(
                "Hello\n",
                "World"
        ), consumer);
    }

    @Test
    void asOutputStream() throws IOException {
        var consumer = new ArrayList<String>();
        try (var out = Lines.asOutputStream(consumer::add, UTF_8)) {
            out.write("Hello\nWorld".getBytes(UTF_8));
        }
        assertEquals(List.of(
                "Hello\n",
                "World"
        ), consumer);
    }
}
