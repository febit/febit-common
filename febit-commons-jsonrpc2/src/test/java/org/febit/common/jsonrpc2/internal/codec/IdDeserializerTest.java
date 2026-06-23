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
package org.febit.common.jsonrpc2.internal.codec;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class IdDeserializerTest {

    private static JsonParser createParser(String json) throws Exception {
        var mapper = new ObjectMapper();
        var parser = mapper.createParser(json);
        // advance to field value token
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE
        return parser;
    }

    @Nested
    class Deserialize {

        @Test
        void stringValue() throws Exception {
            var parser = createParser("""
                    {"id": "abc-123"}
                    """);

            var id = new IdDeserializer().deserialize(parser, null);
            assertNotNull(id);
            assertEquals("abc-123", id.value());
        }

        @Test
        void integerValue() throws Exception {
            var parser = createParser("""
                    {"id": 42}
                    """);

            var id = new IdDeserializer().deserialize(parser, null);
            assertNotNull(id);
            assertEquals(42, id.value());
        }

        @Test
        void longValue() throws Exception {
            var parser = createParser("""
                    {"id": 9999999999}
                    """);

            var id = new IdDeserializer().deserialize(parser, null);
            assertNotNull(id);
            assertEquals(9999999999L, id.value());
        }

        @Test
        void doubleValue() throws Exception {
            var parser = createParser("""
                    {"id": 3.14}
                    """);

            var id = new IdDeserializer().deserialize(parser, null);
            assertNotNull(id);
            assertEquals(3.14, id.value());
        }

        @Test
        void nullValue() throws Exception {
            var parser = createParser("""
                    {"id": null}
                    """);

            var id = new IdDeserializer().deserialize(parser, null);
            assertNull(id);
        }

    }
}
