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
package org.febit.lang.modeler;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.febit.lang.modeler.TestSchemas.S_STRUCT_SIMPLE;
import static org.junit.jupiter.api.Assertions.*;

class StructSpecsTest {

    @Test
    @SuppressWarnings("unchecked")
    void asArray() {
        var modeler = Modeler.builder().structAsArray().build();
        var spec = (StructSpec<Object[], Object[]>) modeler.getStructSpec();

        var struct = (Object[]) modeler.constructStruct(S_STRUCT_SIMPLE, Map.of(
                "id", 1,
                "name", "test"
        ));
        assertArrayEquals(new Object[]{1, "test"}, struct);
        assertEquals(1, spec.get(struct, S_STRUCT_SIMPLE.field("id")));
        assertEquals("test", spec.get(struct, S_STRUCT_SIMPLE.field("name")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void asList() {
        var modeler = Modeler.builder().structAsList().build();
        var spec = (StructSpec<List<Object>, List<Object>>) modeler.getStructSpec();

        var struct = (List<Object>) modeler.constructStruct(S_STRUCT_SIMPLE, Map.of(
                "id", 1,
                "name", "test"
        ));
        assertEquals(List.of(1, "test"), struct);

        assertEquals(1, spec.get(struct, S_STRUCT_SIMPLE.field("id")));
        assertEquals("test", spec.get(struct, S_STRUCT_SIMPLE.field("name")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void asMap() {
        var modeler = Modeler.builder().structAsMap().build();
        var spec = (StructSpec<Map<String, Object>, Map<String, Object>>) modeler.getStructSpec();
        var struct = (Map<String, Object>) modeler.constructStruct(S_STRUCT_SIMPLE, Map.of(
                "id", 1,
                "name", "test"
        ));
        assertEquals(Map.of("id", 1, "name", "test"), struct);
        assertEquals(1, spec.get(struct, S_STRUCT_SIMPLE.field("id")));
        assertEquals("test", spec.get(struct, S_STRUCT_SIMPLE.field("name")));
    }
}
