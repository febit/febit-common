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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.lang.annotation.NullableArgs;
import org.febit.lang.util.jackson.InstantLooseDeserializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.function.Function;

@Data
@Slf4j
@SuppressWarnings({"unused"})
@NoArgsConstructor
@AllArgsConstructor(
        onConstructor_ = {@NullableArgs},
        staticName = "of",
        access = AccessLevel.PACKAGE
)
public class Response<T> implements IMutableResponse<T>, HttpStatusAware {

    private int status;

    private boolean success;
    private String code;
    private String message;

    @JsonDeserialize(using = InstantLooseDeserializer.class)
    private Instant timestamp;

    private T data;

    private static Instant now() {
        return Instant.ofEpochMilli(System.currentTimeMillis());
    }

    @Nonnull
    public static <T> Response<T> success(
            int httpStatus, @Nullable String code, @Nullable String message, @Nullable T data) {
        if (httpStatus > 0 // NOPMD
                && (httpStatus < 200 || httpStatus >= 400) // NOPMD
        ) {
            log.info("HTTP status for successful response is neither [2xx] nor [3xx]: {}", httpStatus);
        }
        return of(httpStatus, true, code, message, now(), data);
    }

    public static <T> Response<T> failed(int httpStatus,
                                         String code, String message, @Nullable T data) {
        return of(httpStatus, false, code, message, now(), data);
    }

    @Nonnull
    public <D> Response<D> transferData(@Nonnull Function<T, D> action) {
        var target = new Response<D>();
        target.copyProperties(this);
        target.setData(action.apply(getData()));
        return target;
    }

    protected void copyProperties(@Nonnull IResponse<?> from) {
        this.setStatus(from.getStatus());
        setSuccess(from.isSuccess());
        setCode(from.getCode());
        setMessage(from.getMessage());
        setTimestamp(from.getTimestamp());
    }
}
