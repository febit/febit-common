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

/**
 * JSON-RPC 2.0 Reserved errors.
 * <p>
 * NOTICE: -32000 to -32099 Server error Reserved for implementation-defined server-errors.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ReservedRpcErrors {

    //-32000 to -32099 	Server error 	Reserved for implementation-defined server-errors.
    RESERVED_00(
            -32000, "Server error 32000",
            "Reserved for implementation-defined server-errors"
    ),
    RESERVED_99(
            -32099, "Server error 32099",
            "Reserved for implementation-defined server-errors"
    ),
    ;

    private final int code;
    private final String message;
    private final String description;
}
