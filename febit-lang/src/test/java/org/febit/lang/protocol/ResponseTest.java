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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    private static Response<String> buildResponse(int status, boolean success, String code, String message, Instant ts, String data) {
        var r = new Response<String>();
        r.setStatus(status);
        r.setSuccess(success);
        r.setCode(code);
        r.setMessage(message);
        r.setTimestamp(ts);
        r.setData(data);
        return r;
    }

    @Test
    void jsonify() {
        var original = Response.ok(200, "code", "message", "data");
        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                Response.class
        );
        assertEquals(original, parsed);

        assertThat(JacksonUtils.toNamedMap(original))
                .containsEntry("status", 200)
                .containsEntry("success", true)
                .containsEntry("code", "code")
                .containsEntry("message", "message")
                .containsEntry("data", "data")
                .containsKey("timestamp")
                .doesNotContainKeys(
                        "failed",
                        "successWithStatus",
                        "failedWithStatus",
                        "present",
                        "empty"
                );
    }

    @Test
    void transform() {
        var original = Response.ok(200, "code", "message", "data");

        assertThat(original.map(d -> d + "1"))
                .returns("data1", IResponse::getData);

        assertThat(original.mapIfPresent(d -> 1))
                .returns(1, IResponse::getData);

        assertThat(original.cleanData())
                .returns(null, IResponse::getData);

        assertThat(original.or(() -> "x"))
                .returns("data", IResponse::getData);
        assertThat(original.orElse("x"))
                .isEqualTo("data");
        assertThat(original.orElseGet(() -> "x"))
                .isEqualTo("data");

        assertThat(original.cleanData().or(() -> "x"))
                .returns("x", IResponse::getData);
        assertThat(original.cleanData().orElse("x"))
                .isEqualTo("x");
        assertThat(original.cleanData().orElseGet(() -> "x"))
                .isEqualTo("x");
    }

    @Test
    void noArgsConstructor_setsAllDefaults() {
        var r = new Response<String>();
        assertEquals(0, r.getStatus());
        assertFalse(r.isSuccess());
        assertNull(r.getCode());
        assertNull(r.getMessage());
        assertNull(r.getTimestamp());
        assertNull(r.getData());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        var ts = Instant.now();
        var r = buildResponse(200, true, "C", "m", ts, "d");
        assertEquals(200, r.getStatus());
        assertTrue(r.isSuccess());
        assertEquals("C", r.getCode());
        assertEquals("m", r.getMessage());
        assertEquals(ts, r.getTimestamp());
        assertEquals("d", r.getData());
    }

    @Test
    void setters_roundTrip() {
        var r = new Response<String>();
        var ts = Instant.now();
        r.setStatus(201);
        r.setSuccess(true);
        r.setCode("C");
        r.setMessage("m");
        r.setTimestamp(ts);
        r.setData("d");
        assertEquals(201, r.getStatus());
        assertTrue(r.isSuccess());
        assertEquals("C", r.getCode());
        assertEquals("m", r.getMessage());
        assertEquals(ts, r.getTimestamp());
        assertEquals("d", r.getData());
    }

    @Test
    void setters_acceptNull() {
        var r = buildResponse(200, true, "C", "m", Instant.now(), "d");
        r.setCode(null);
        r.setMessage(null);
        r.setTimestamp(null);
        r.setData(null);
        assertNull(r.getCode());
        assertNull(r.getMessage());
        assertNull(r.getTimestamp());
        assertNull(r.getData());
    }

    @Test
    void ok_setsSuccessTrueAndStampsNow() {
        var before = Instant.now();
        var r = Response.ok(200, "C", "m", "d");
        var after = Instant.now();
        assertEquals(200, r.getStatus());
        assertTrue(r.isSuccess());
        assertNotNull(r.getTimestamp());
        // Response.now() uses System.currentTimeMillis() which has millisecond
        // precision. Truncate both bounds to millis for stable comparison.
        var ts = r.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        assertFalse(ts.isBefore(before.truncatedTo(java.time.temporal.ChronoUnit.MILLIS)));
        assertFalse(ts.isAfter(after.plusMillis(1).truncatedTo(java.time.temporal.ChronoUnit.MILLIS)));
    }

    @Test
    void ok_withNon2xxStatus_stillSuccessAndLogs() {
        // 4xx/5xx is a "success" response (caller signaled business-OK),
        // but ok() will log a debug message. We just verify the field.
        var r = Response.ok(500, "C", "m", "d");
        assertTrue(r.isSuccess());
        assertEquals(500, r.getStatus());
    }

    @Test
    void ok_withZeroStatus_stillSuccess() {
        var r = Response.ok(0, "C", "m", "d");
        assertEquals(0, r.getStatus());
        assertTrue(r.isSuccess());
    }

    @Test
    void ok_withNegativeStatus_skipsDebugLog() {
        // The guard is `httpStatus > 0 && (status < 200 || status >= 400)`,
        // so negative status is NOT logged but still success.
        var r = Response.ok(-1, "C", "m", "d");
        assertTrue(r.isSuccess());
        assertEquals(-1, r.getStatus());
    }

    @Test
    void ok_withNullData() {
        var r = Response.ok(200, "C", "m", null);
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void ok_withNullCodeAndMessage() {
        var r = Response.ok(200, null, null, "d");
        assertNull(r.getCode());
        assertNull(r.getMessage());
    }

    @Test
    void failed_setsSuccessFalseAndStampsNow() {
        var before = Instant.now();
        var r = Response.failed(500, "C", "m", null);
        var after = Instant.now();
        assertEquals(500, r.getStatus());
        assertFalse(r.isSuccess());
        assertNotNull(r.getTimestamp());
        var ts = r.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        assertFalse(ts.isBefore(before.truncatedTo(java.time.temporal.ChronoUnit.MILLIS)));
        assertFalse(ts.isAfter(after.plusMillis(1).truncatedTo(java.time.temporal.ChronoUnit.MILLIS)));
    }

    @Test
    void failed_withData_keepsData() {
        var r = Response.failed(500, "C", "m", "payload");
        assertEquals("payload", r.getData());
        assertFalse(r.isSuccess());
    }

    @Test
    void failed_withZeroStatus() {
        var r = Response.failed(0, "C", "m", null);
        assertEquals(0, r.getStatus());
        assertFalse(r.isSuccess());
    }

    @Test
    void map_returnsResponseInstance() {
        var original = Response.ok(200, "C", "m", "d");
        var mapped = original.map(s -> s + "!");
        assertNotNull(mapped);
        assertEquals("d!", mapped.getData());
    }

    @Test
    void map_copiesAllResponseProperties() {
        var r = Response.ok(201, "C", "m", "old");
        var ts = r.getTimestamp();
        var mapped = r.map(d -> d + "!");
        assertEquals(201, mapped.getStatus());
        assertTrue(mapped.isSuccess());
        assertEquals("C", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals(ts, mapped.getTimestamp());
    }

    @Test
    void map_mappingReturningNull_yieldsNullData() {
        var r = Response.ok(200, null, null, "v");
        var mapped = r.map(d -> null);
        assertNull(mapped.getData());
        assertEquals(200, mapped.getStatus());
    }

    @Test
    void map_dataNull_stillInvokesMapping() {
        var r = buildResponse(200, true, "C", "m", null, null);
        var mapped = r.map(d -> "fallback");
        assertEquals("fallback", mapped.getData());
    }

    @Test
    void copyProperties_isInvokedByMap() {
        // copyProperties is package-private; verified indirectly via map()
        var source = Response.failed(503, "DOWN", "service down", "x");
        var mapped = source.map(d -> d + "_mapped");
        assertEquals(503, mapped.getStatus());
        assertFalse(mapped.isSuccess());
        assertEquals("DOWN", mapped.getCode());
        assertEquals("service down", mapped.getMessage());
        assertEquals("x_mapped", mapped.getData());
    }

    @Test
    void isPresent_whenDataIsNull() {
        var r = buildResponse(200, true, null, null, null, null);
        assertFalse(r.isPresent());
        assertTrue(r.isEmpty());
    }

    @Test
    void isPresent_whenDataIsNotNull() {
        var r = buildResponse(200, true, null, null, null, "x");
        assertTrue(r.isPresent());
        assertFalse(r.isEmpty());
    }

    @Test
    void isFailed_followsSuccess() {
        var ok = buildResponse(200, true, null, null, null, null);
        var fail = buildResponse(500, false, null, null, null, null);
        assertFalse(ok.isFailed());
        assertTrue(fail.isFailed());
    }

    @Test
    void isSuccessWithStatus_bothMustMatch() {
        var r = buildResponse(200, true, null, null, null, null);
        assertTrue(r.isSuccessWithStatus(200));
        assertFalse(r.isSuccessWithStatus(201));
    }

    @Test
    void isFailedWithStatus_bothMustMatch() {
        var r = buildResponse(404, false, null, null, null, null);
        assertTrue(r.isFailedWithStatus(404));
        assertFalse(r.isFailedWithStatus(200));
    }

    @Test
    void or_dataPresent_returnsSelf() {
        var r = Response.ok(200, "C", "m", "d");
        assertSame(r, r.or(() -> "x"));
    }

    @Test
    void or_dataNull_callsSupplier() {
        var r = buildResponse(200, true, null, null, null, null);
        var result = r.or(() -> "fallback");
        assertEquals("fallback", result.getData());
    }

    @Test
    void orElse_dataNull_returnsOther() {
        var r = buildResponse(200, true, null, null, null, null);
        assertEquals("other", r.orElse("other"));
    }

    @Test
    void orElseGet_dataNull_callsSupplier() {
        var r = buildResponse(200, true, null, null, null, null);
        assertEquals("fb", r.orElseGet(() -> "fb"));
    }

    @Test
    void mapIfPresent_dataNull_noInvoke() {
        var r = buildResponse(200, true, null, null, null, null);
        var mapped = r.mapIfPresent(d -> d + "!");
        assertNull(mapped.getData());
    }

    @Test
    void mapIfPresent_dataPresent_invokes() {
        var r = Response.ok(200, "C", "m", "v");
        var mapped = r.mapIfPresent(String::toUpperCase);
        assertEquals("V", mapped.getData());
    }

    @Test
    void cleanData_preservesEverythingExceptData() {
        var r = Response.ok(201, "C", "m", "d");
        var ts = r.getTimestamp();
        var cleaned = r.cleanData();
        assertEquals(201, cleaned.getStatus());
        assertTrue(cleaned.isSuccess());
        assertEquals("C", cleaned.getCode());
        assertEquals("m", cleaned.getMessage());
        assertEquals(ts, cleaned.getTimestamp());
        assertNull(cleaned.getData());
    }

    @Test
    void jsonify_failedResponse_roundTrips() {
        var original = Response.failed(404, "NOT_FOUND", "missing", null);
        var parsed = JacksonUtils.parse(JacksonUtils.toJsonString(original), Response.class);
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_withNullDataInJson() {
        String json = """
                {
                    "success": true,
                    "status": 200
                }
                """;
        var parsed = JacksonUtils.parse(json, Response.class);
        assertNotNull(parsed);
        assertNull(parsed.getData());
        assertTrue(parsed.isSuccess());
        assertEquals(200, parsed.getStatus());
    }

    @Test
    void jsonify_includesAllFields() {
        var original = Response.ok(201, "CODE", "msg", "payload");
        var map = JacksonUtils.toNamedMap(original);
        assertNotNull(map);
        assertEquals(201, map.get("status"));
        assertEquals(true, map.get("success"));
        assertEquals("CODE", map.get("code"));
        assertEquals("msg", map.get("message"));
        assertEquals("payload", map.get("data"));
        assertNotNull(map.get("timestamp"));
    }

    @Test
    void equalsAndHashCode_basedOnDataAnnotation() {
        var ts = Instant.now();
        var a = buildResponse(200, true, "C", "m", ts, "d");
        var b = buildResponse(200, true, "C", "m", ts, "d");
        var c = buildResponse(200, true, "C", "m", ts, "different");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_doesNotThrow() {
        var str = Response.ok(200, "C", "m", "d").toString();
        assertNotNull(str);
    }
}
