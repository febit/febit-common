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
package org.febit.lang.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IBasicApiTest {

    static class SampleApi implements IBasicApi {
    }

    @Test
    void ok_produces200WithNullData() {
        var api = new SampleApi();
        var r = api.ok();
        assertEquals(200, r.getStatus());
        assertTrue(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void ok_withData_returnsData() {
        var api = new SampleApi();
        var r = api.ok("payload");
        assertEquals(200, r.getStatus());
        assertTrue(r.isSuccess());
        assertEquals("payload", r.getData());
    }

    @Test
    void ok_genericType_preserves() {
        var api = new SampleApi();
        Integer number = 42;
        var r = api.ok(number);
        assertEquals(42, r.getData());
    }

    @Test
    void ok_nullDataArgument_isSuccess() {
        var api = new SampleApi();
        var r = api.ok((Object) null);
        assertEquals(200, r.getStatus());
        assertTrue(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void created_returns201() {
        var api = new SampleApi();
        var r = api.created();
        assertEquals(201, r.getStatus());
        assertTrue(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void accepted_returns202() {
        var api = new SampleApi();
        var r = api.accepted();
        assertEquals(202, r.getStatus());
        assertTrue(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void created_isNotFailed() {
        var api = new SampleApi();
        var r = api.created();
        assertFalse(r.isFailed());
    }

    @Test
    void allDefaults_returnNonNullResponse() {
        var api = new SampleApi();
        assertNotNull(api.ok());
        assertNotNull(api.ok("x"));
        assertNotNull(api.created());
        assertNotNull(api.accepted());
    }

    @Test
    void defaultMethods_areInheritedByImplementor() {
        // Implementor without any custom code should still produce IResponse instances
        IBasicApi api = new SampleApi();
        assertNotNull(api.ok());
    }
}
