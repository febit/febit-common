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
package org.febit.common.jsonrpc2.exception;

import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcErrorExceptionTest {

    @Test
    void constructWithError() {
        var error = StdRpcErrors.PARSE_ERROR.toError();
        var ex = new RpcErrorException(error);
        assertEquals(error, ex.getError());
        assertNull(ex.getCause());
        assertEquals("[-32700] Parse error", ex.getMessage());
    }

    @Test
    void constructWithErrorAndCause() {
        var error = StdRpcErrors.INTERNAL_ERROR.toError("boom");
        var cause = new RuntimeException("root");
        var ex = new RpcErrorException(error, cause);
        assertEquals(error, ex.getError());
        assertEquals(cause, ex.getCause());
        assertEquals("[-32603] boom", ex.getMessage());
    }

    @Test
    void messageFormatting() {
        var error = StdRpcErrors.INVALID_PARAMS.toError("bad params");
        var ex = new RpcErrorException(error);
        assertEquals("[-32602] bad params", ex.getMessage());
    }

    @Test
    void nullCauseAllowed() {
        var error = StdRpcErrors.METHOD_NOT_FOUND.toError();
        var ex = new RpcErrorException(error, null);
        assertNull(ex.getCause());
    }
}
