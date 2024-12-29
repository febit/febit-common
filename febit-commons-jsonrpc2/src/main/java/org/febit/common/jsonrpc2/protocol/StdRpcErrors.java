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
package org.febit.common.jsonrpc2.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.febit.common.jsonrpc2.RpcErrors;

/**
 * JSON-RPC 2.0 Specification errors.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum StdRpcErrors implements RpcErrors {

    INVALID_REQUEST(
            -32600, "Invalid Request",
            "The JSON sent is not a valid Request object."
    ),
    METHOD_NOT_FOUND(
            -32601, "Method not found",
            "The method does not exist / is not available."
    ),
    INVALID_PARAMS(
            -32602, "Invalid params",
            "Invalid method parameter(s)."
    ),
    INTERNAL_ERROR(
            -32603, "Internal error",
            "Internal JSON-RPC error."
    ),
    PARSE_ERROR(
            -32700, "Parse error",
            "Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text."
    ),
    ;

    private final int code;
    private final String message;
    private final String description;

}
