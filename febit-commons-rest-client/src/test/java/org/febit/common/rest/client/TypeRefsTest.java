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
package org.febit.common.rest.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeRefsTest {

    @Test
    void forResponseShouldCreateParameterizedTypeReference() {
        var ref = TypeRefs.forResponse(String.class);
        assertNotNull(ref);

        var type = ref.getType();
        assertNotNull(type);
        // Should be ParameterizedType with IResponse<String>
        var typeStr = type.toString();
        assertTrue(typeStr.contains("IResponse"));
        assertTrue(typeStr.contains("String"));
    }

    @Test
    void forResponseShouldCreateDifferentTypesForDifferentArgs() {
        var stringRef = TypeRefs.forResponse(String.class);
        var intRef = TypeRefs.forResponse(Integer.class);

        assertNotEquals(stringRef.getType(), intRef.getType());
    }

    @Test
    void forResponseShouldWorkWithSpecialClasses() {
        assertNotNull(TypeRefs.forResponse(Void.class));
        assertNotNull(TypeRefs.forResponse(Object.class));
    }

    @Test
    void forResponseShouldReturnConsistentTypeForSameArg() {
        var ref1 = TypeRefs.forResponse(Integer.class);
        var ref2 = TypeRefs.forResponse(Integer.class);

        assertNotNull(ref1);
        assertNotNull(ref2);
        assertEquals(ref1.getType(), ref2.getType());
    }
}
