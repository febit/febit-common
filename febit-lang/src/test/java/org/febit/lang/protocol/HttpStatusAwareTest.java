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
import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusAwareTest {

    static class Holder implements HttpStatusAware {
        int status;

        @Override
        public void setStatus(int status) {
            this.status = status;
        }
    }

    @TableTest("""
            status | expected
            404    | 404
            0      | 0
            -1     | -1
            """)
    void setStatus_writesValue(int status, int expected) {
        var h = new Holder();
        h.setStatus(status);
        assertEquals(expected, h.status);
    }

    @Test
    void setStatus_overwritesPreviousValue() {
        var h = new Holder();
        h.setStatus(200);
        h.setStatus(500);
        assertEquals(500, h.status);
    }

    @Test
    void responseImplementsHttpStatusAware() {
        var aware = new Response<String>();
        assertNotNull(aware);
        aware.setStatus(418);
        assertEquals(418, aware.getStatus());
    }
}
