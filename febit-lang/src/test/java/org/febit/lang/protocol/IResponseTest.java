package org.febit.lang.protocol;

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IResponseTest {

    @Test
    void jsonify() {
        var original = IResponse.success(200, "code", "message", "data");
        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                IResponse.class
        );
        assertEquals(original, parsed);
    }

    @Test
    void factory() {
        assertThat(IResponse.success())
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns(null, IResponse::getData);

        assertThat(IResponse.success("data"))
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.success(201, "data"))
                .returns(201, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getCode)
                .returns(null, IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.success("code", "message", "data"))
                .returns(200, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.success(202, "code", "message", "data"))
                .returns(202, IResponse::getStatus)
                .returns(true, IResponse::isSuccess)
                .returns("code", IResponse::getCode)
                .returns("message", IResponse::getMessage)
                .returns("data", IResponse::getData);

        assertThat(IResponse.success(499, "code", "message", "data"))
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
        assertThat(Response.success(200, "code", "message", "data"))
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

        assertThat(Response.success(201, null, null, null))
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

        assertThat(Response.success(300, "code", "message", "data"))
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
