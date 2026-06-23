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
package org.febit.common.jsonrpc2.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcParamsKindTest {

    @Test
    void firstArgumentExists() {
        assertEquals(RpcParamsKind.FIRST_ARGUMENT, RpcParamsKind.valueOf("FIRST_ARGUMENT"));
    }

    @Test
    void flattenListExists() {
        assertEquals(RpcParamsKind.FLATTEN_LIST, RpcParamsKind.valueOf("FLATTEN_LIST"));
    }

    @Test
    void flattenObjectExists() {
        assertEquals(RpcParamsKind.FLATTEN_OBJECT, RpcParamsKind.valueOf("FLATTEN_OBJECT"));
    }

    @Test
    void hasThreeValues() {
        assertEquals(3, RpcParamsKind.values().length);
    }
}
