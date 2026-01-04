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

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IResponseTest {

    @Test
    void jsonify() {
        var original = IResponse.ok(200, "code", "message", "data");
        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                IResponse.class
        );
        assertEquals(original, parsed);
    }

    @Test
    void factory() {
        assertThat(IResponse.ok())
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns(null, IResponse::getData);

        assertThat(IResponse.ok("data"))
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.ok(201, "data"))
                .returns(201, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.ok("code", "message", "data"))
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.ok(202, "code", "message", "data"))
                .returns(202, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.ok(499, "code", "message", "data"))
                .returns(499, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.failed("code", "message"))
                .returns(500, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns(null, IResponse::getData);

        assertThat(IResponse.failed(401, "code", "message"))
                .returns(401, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns(null, IResponse::getData);

        assertThat(IResponse.failed("code", "message", "data"))
                .returns(500, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.failed(402, "code", "message", "data"))
                .returns(402, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);
    }

    @Test
    void props() {
        assertThat(Response.ok(200, "code", "message", "data"))
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData)
                .returns(true, IResponse::isPresent)
                .returns(false, IResponse::isEmpty)
                .returns(false, IResponse::isFailed)
                .returns(true, r -> r.isSuccessWithStatus(200))
                .returns(false, r -> r.isSuccessWithStatus(400))
                .returns(false, r -> r.isFailedWithStatus(200))
                .returns(false, r -> r.isFailedWithStatus(400));

        assertThat(Response.ok(201, null, null, null))
                .returns(201, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns(null, IResponse::getData)
                .returns(false, IResponse::isPresent)
                .returns(true, IResponse::isEmpty)
                .returns(false, IResponse::isFailed)
                .returns(true, r -> r.isSuccessWithStatus(201))
                .returns(false, r -> r.isSuccessWithStatus(400))
                .returns(false, r -> r.isFailedWithStatus(201))
                .returns(false, r -> r.isFailedWithStatus(400));

        assertThat(Response.ok(300, "code", "message", "data"))
                .returns(300, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("data", IResponse::getData)
                .returns(true, IResponse::isPresent)
                .returns(false, IResponse::isEmpty)
                .returns(false, IResponse::isFailed)
                .returns(true, r -> r.isSuccessWithStatus(300))
                .returns(false, r -> r.isSuccessWithStatus(400))
                .returns(false, r -> r.isFailedWithStatus(300))
                .returns(false, r -> r.isFailedWithStatus(400));

        assertThat(Response.failed(401, "code", "message", "data"))
                .returns(401, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("data", IResponse::getData)
                .returns(true, IResponse::isPresent)
                .returns(false, IResponse::isEmpty)
                .returns(true, IResponse::isFailed)
                .returns(false, r -> r.isSuccessWithStatus(401))
                .returns(false, r -> r.isSuccessWithStatus(400))
                .returns(true, r -> r.isFailedWithStatus(401))
                .returns(false, r -> r.isFailedWithStatus(400));

        assertThat(Response.failed(502, "SERVER_ERROR", "Internal Server Error", null))
                .returns(502, IResponse::getStatus)
                .returns(false, IResponse::isSuccess)
                .returns("SERVER_ERROR", IResponse::getCode)
                .returns("Internal Server Error", IResponse::getMessage)
                .returns(null, IResponse::getData)
                .returns(false, IResponse::isPresent)
                .returns(true, IResponse::isEmpty)
                .returns(true, IResponse::isFailed)
                .returns(false, r -> r.isSuccessWithStatus(502))
                .returns(false, r -> r.isSuccessWithStatus(400))
                .returns(true, r -> r.isFailedWithStatus(502))
                .returns(false, r -> r.isFailedWithStatus(400));

    }

}
