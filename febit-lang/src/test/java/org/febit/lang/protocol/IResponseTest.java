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

import org.febit.lang.jackson.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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
    void ok_zeroStatus_stillSuccess() {
        var r = IResponse.ok(0, "x");
        assertEquals(0, r.getStatus());
        assertTrue(r.isSuccess());
    }

    @Test
    void ok_negativeStatus_stillSuccess() {
        var r = IResponse.ok(-1, "x");
        assertEquals(-1, r.getStatus());
        assertTrue(r.isSuccess());
    }

    @Test
    void ok_nullData_returnsResponseWithNullData() {
        var r = IResponse.ok((Object) null);
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void ok_dataIsEmptyString_isPresent() {
        // isPresent() depends on data != null, not on data being non-empty
        var r = IResponse.ok("");
        assertTrue(r.isPresent());
        assertFalse(r.isEmpty());
    }

    @Test
    void ok_dataIsZero_isPresent() {
        // 0 is not null
        var r = IResponse.ok(0);
        assertTrue(r.isPresent());
        assertFalse(r.isEmpty());
    }

    @Test
    void ok_dataIsEmptyList_isPresent() {
        var r = IResponse.ok(java.util.List.of());
        assertTrue(r.isPresent());
        assertFalse(r.isEmpty());
    }

    @Test
    void failed_zeroStatus_isFailed() {
        var r = IResponse.failed(0, "C", "m");
        assertEquals(0, r.getStatus());
        assertFalse(r.isSuccess());
        assertTrue(r.isFailed());
    }

    @Test
    void failed_negativeStatus_isFailed() {
        var r = IResponse.failed(-1, "C", "m");
        assertEquals(-1, r.getStatus());
        assertTrue(r.isFailed());
    }

    @Test
    void failed_nullData_allowed() {
        var r = IResponse.failed("C", "m", null);
        assertNull(r.getData());
        assertTrue(r.isFailed());
    }

    @Test
    void alias_status_matchesGetStatus() {
        var r = IResponse.ok(201, "x");
        assertEquals(r.getStatus(), r.status());
    }

    @Test
    void alias_code_matchesGetCode() {
        var r = IResponse.ok("CODE", "msg", "d");
        assertEquals("CODE", r.code());
        assertEquals(r.getCode(), r.code());
    }

    @Test
    void alias_message_matchesGetMessage() {
        var r = IResponse.ok("C", "hello", "d");
        assertEquals("hello", r.message());
        assertEquals(r.getMessage(), r.message());
    }

    @Test
    void alias_data_matchesGetData() {
        var r = IResponse.ok("data");
        assertEquals("data", r.data());
        assertEquals(r.getData(), r.data());
    }

    @Test
    void alias_timestamp_matchesGetTimestamp() {
        var r = IResponse.ok(200, "c", "m", "d");
        assertNotNull(r.getTimestamp());
        assertEquals(r.getTimestamp(), r.timestamp());
    }

    @Test
    void alias_timestamp_isCloseToNow() {
        var before = Instant.now();
        var r = IResponse.ok();
        var after = Instant.now();
        assertNotNull(r.timestamp());
        assertFalse(r.timestamp().isBefore(before.minusSeconds(1)));
        assertFalse(r.timestamp().isAfter(after.plusSeconds(1)));
    }

    @Test
    void isFailed_isFalseWhenSuccess() {
        var r = IResponse.ok(200, "c", "m", "d");
        assertTrue(r.isSuccess());
        assertFalse(r.isFailed());
    }

    @Test
    void isFailed_isTrueWhenFailed() {
        var r = IResponse.failed(500, "C", "m");
        assertFalse(r.isSuccess());
        assertTrue(r.isFailed());
    }

    @Test
    void isSuccessWithStatus_trueOnlyWhenBothMatch() {
        var r = IResponse.ok(200, "c", "m", "d");
        assertTrue(r.isSuccessWithStatus(200));
        assertFalse(r.isSuccessWithStatus(201));
        assertFalse(r.isSuccessWithStatus(500));
    }

    @Test
    void isSuccessWithStatus_falseForFailedResponse() {
        var r = IResponse.failed(200, "C", "m");
        // status matches but success is false
        assertFalse(r.isSuccessWithStatus(200));
    }

    @Test
    void isFailedWithStatus_trueOnlyWhenFailedAndStatusMatches() {
        var r = IResponse.failed(404, "C", "m");
        assertTrue(r.isFailedWithStatus(404));
        assertFalse(r.isFailedWithStatus(200));
        assertFalse(r.isFailedWithStatus(500));
    }

    @Test
    void isFailedWithStatus_falseForSuccessResponse() {
        var r = IResponse.ok(404, "c", "m", "d");
        // status matches but success is true
        assertFalse(r.isFailedWithStatus(404));
    }

    @Test
    void isPresent_falseWhenDataIsNull() {
        var r = IResponse.ok();
        assertFalse(r.isPresent());
        assertTrue(r.isEmpty());
    }

    @Test
    void isPresent_trueWhenDataIsNotNull() {
        var r = IResponse.ok("x");
        assertTrue(r.isPresent());
        assertFalse(r.isEmpty());
    }

    @Test
    void isPresent_isPresentAndIsEmpty_areOpposite() {
        var withData = IResponse.ok("x");
        var withoutData = IResponse.ok();
        assertTrue(withData.isPresent());
        assertFalse(withData.isEmpty());
        assertFalse(withoutData.isPresent());
        assertTrue(withoutData.isEmpty());
    }

    @Test
    void cleanData_preservesStatusCodeMessageTimestamp() {
        var r = Response.ok(201, "C", "m", "data");
        var originalTimestamp = r.getTimestamp();
        var cleaned = r.cleanData();
        assertEquals(201, cleaned.getStatus());
        assertEquals("C", cleaned.getCode());
        assertEquals("m", cleaned.getMessage());
        assertNull(cleaned.getData());
        assertEquals(originalTimestamp, cleaned.getTimestamp());
    }

    @Test
    void cleanData_onEmptyResponse_yieldsSameStatusEtc() {
        var r = IResponse.ok();
        var cleaned = r.cleanData();
        assertEquals(200, cleaned.getStatus());
        assertTrue(cleaned.isSuccess());
        assertNull(cleaned.getData());
    }

    @Test
    void map_preservesAllResponseProperties() {
        var r = Response.ok(201, "C", "m", "old");
        var ts = r.getTimestamp();
        var mapped = r.map(d -> d + "!");
        assertEquals(201, mapped.getStatus());
        assertTrue(mapped.isSuccess());
        assertEquals("C", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals("old!", mapped.getData());
        assertEquals(ts, mapped.getTimestamp());
    }

    @Test
    void map_withNullData_stillInvokesMapping() {
        var counter = new AtomicInteger();
        var r = IResponse.<String>ok();
        var mapped = r.map(d -> {
            counter.incrementAndGet();
            return "default";
        });
        assertEquals(1, counter.get());
        assertEquals("default", mapped.getData());
    }

    @Test
    void map_returningNull_yieldsNullData() {
        var r = IResponse.ok("x");
        var mapped = r.map(d -> null);
        assertNull(mapped.getData());
        assertEquals(200, mapped.getStatus());
    }

    @Test
    void mapIfPresent_dataNull_doesNotInvokeMapping() {
        var counter = new AtomicInteger();
        var r = IResponse.<String>ok();
        var mapped = r.mapIfPresent(d -> {
            counter.incrementAndGet();
            return "x";
        });
        assertEquals(0, counter.get(), "mapping should not be invoked when data is null");
        assertNull(mapped.getData());
    }

    @Test
    void mapIfPresent_dataPresent_invokesMapping() {
        var r = IResponse.ok("v");
        var mapped = r.mapIfPresent(String::toUpperCase);
        assertEquals("V", mapped.getData());
    }

    @Test
    void mapIfPresent_dataPresent_mappingReturnsNull() {
        var r = IResponse.ok("v");
        var mapped = r.mapIfPresent(d -> null);
        assertNull(mapped.getData());
    }

    @Test
    void or_dataPresent_returnsSelf() {
        var r = IResponse.ok("x");
        var result = r.or(() -> "fallback");
        assertSame(r, result);
    }

    @Test
    void or_dataNull_invokesSupplier() {
        var r = IResponse.<String>ok();
        var result = r.or(() -> "fallback");
        assertEquals("fallback", result.getData());
        assertTrue(result.isPresent());
    }

    @Test
    void or_supplierReturningNull_yieldsNullData() {
        var r = IResponse.<String>ok();
        var result = r.or(() -> null);
        assertNull(result.getData());
        assertFalse(result.isPresent());
    }

    @Test
    void or_nullSupplier_throwsNpe() {
        var r = IResponse.ok("x");
        assertThrows(NullPointerException.class, () -> r.or(null));
    }

    @Test
    void or_dataNull_nullSupplier_throwsNpe() {
        var r = IResponse.<String>ok();
        // isPresent() is false, so it would call map(d -> supplier.get()) →
        // Objects.requireNonNull(supplier) on null
        assertThrows(NullPointerException.class, () -> r.or(null));
    }

    @Test
    void orElse_dataPresent_returnsData() {
        var r = IResponse.ok("actual");
        assertEquals("actual", r.orElse("fallback"));
    }

    @Test
    void orElse_dataNull_returnsOther() {
        var r = IResponse.<String>ok();
        assertEquals("fallback", r.orElse("fallback"));
    }

    @Test
    void orElse_dataNull_otherAlsoNull_returnsNull() {
        var r = IResponse.<String>ok();
        assertNull(r.orElse(null));
    }

    @Test
    void orElseGet_dataPresent_doesNotInvokeSupplier() {
        var counter = new AtomicInteger();
        var r = IResponse.ok("x");
        var result = r.orElseGet(() -> {
            counter.incrementAndGet();
            return "fallback";
        });
        assertEquals("x", result);
        assertEquals(0, counter.get());
    }

    @Test
    void orElseGet_dataNull_invokesSupplier() {
        var r = IResponse.<String>ok();
        var result = r.orElseGet(() -> "fallback");
        assertEquals("fallback", result);
    }

    @Test
    void orElseGet_dataNull_supplierReturnsNull() {
        var r = IResponse.<String>ok();
        assertNull(r.orElseGet(() -> null));
    }

    @Test
    void orElseGet_nullSupplier_throwsNpe() {
        var r = IResponse.ok("x");
        assertThrows(NullPointerException.class, () -> r.orElseGet(null));
    }

    @Test
    void orElseGet_dataNull_nullSupplier_throwsNpe() {
        var r = IResponse.<String>ok();
        assertThrows(NullPointerException.class, () -> r.orElseGet(null));
    }

    @Test
    void getTimestamp_isSetOnOk() {
        var r = IResponse.ok();
        assertNotNull(r.getTimestamp());
    }

    @Test
    void getTimestamp_isSetOnFailed() {
        var r = IResponse.failed("C", "m");
        assertNotNull(r.getTimestamp());
    }

    @Test
    void map_chainsToAnotherMap() {
        var r = IResponse.ok(1);
        var mapped = r.map(i -> i * 10).map(i -> "v=" + i);
        assertEquals("v=10", mapped.getData());
    }

    @Test
    void failed_withDataAndMatchingStatus_keepsData() {
        var r = IResponse.failed(500, "C", "m", "payload");
        assertEquals("payload", r.getData());
        assertEquals(500, r.getStatus());
    }

    @Test
    void failed_isSuccessIsFalse() {
        var r = IResponse.failed("C", "m");
        assertFalse(r.isSuccess());
    }

    @Test
    void ok_isSuccessIsTrue() {
        var r = IResponse.ok("d");
        assertTrue(r.isSuccess());
    }

    @Test
    void failed_isFailedIsTrue() {
        var r = IResponse.failed("C", "m");
        assertTrue(r.isFailed());
    }

    @Test
    void isFailed_consistentWithIsSuccess() {
        // isFailed() is the inverse of isSuccess()
        var ok = IResponse.ok("d");
        var fail = IResponse.failed("C", "m");
        assertEquals(!ok.isSuccess(), ok.isFailed());
        assertEquals(!fail.isSuccess(), fail.isFailed());
    }

    @Test
    void isFailed_overrideAppliesToConcreteImpl() {
        // Response.isFailed() comes from IResponse default; verify on Response
        var ok = Response.ok(200, null, null, null);
        var fail = Response.failed(500, "C", "m", null);
        assertFalse(ok.isFailed());
        assertTrue(fail.isFailed());
    }

    @Test
    void jsonify_failedRoundTrips() {
        var original = IResponse.failed(404, "NOT_FOUND", "missing");
        var json = JacksonUtils.toJsonString(original);
        var parsed = JacksonUtils.parse(json, IResponse.class);
        assertEquals(original, parsed);
    }
}
