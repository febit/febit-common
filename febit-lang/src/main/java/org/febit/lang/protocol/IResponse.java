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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.febit.lang.annotation.NonNullArgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.function.Function;

@JsonDeserialize(as = Response.class)
public interface IResponse<T> extends Fallible {

    static <T> IResponse<T> success() {
        return success(200, null);
    }

    @Nonnull
    static <T> IResponse<T> success(@Nullable T data) {
        return success(200, data);
    }

    @Nonnull
    static <T> IResponse<T> success(int httpStatus, @Nullable T data) {
        return success(httpStatus, null, null, data);
    }

    @Nonnull
    static <T> IResponse<T> success(@Nullable String code, @Nullable String message, @Nullable T data) {
        return success(200, code, message, data);
    }

    @Nonnull
    static <T> IResponse<T> success(int httpStatus, @Nullable String code, @Nullable String message, @Nullable T data) {
        return Response.success(httpStatus, code, message, data);
    }

    @Nonnull
    @NonNullArgs
    static <T> IResponse<T> failed(String code, String message) {
        return failed(code, message, null);
    }

    @Nonnull
    @NonNullArgs
    static <T> IResponse<T> failed(int httpStatus, String code, String message) {
        return failed(httpStatus, code, message, null);
    }

    @Nonnull
    @NonNullArgs
    static <T> IResponse<T> failed(String code, String message, @Nullable T data) {
        return failed(500, code, message, data);
    }

    @Nonnull
    @NonNullArgs
    static <T> IResponse<T> failed(int httpStatus,
                                   String code, String message, @Nullable T data) {
        return Response.failed(httpStatus, code, message, data);
    }

    int getStatus();

    boolean isSuccess();

    String getCode();

    String getMessage();

    @Nullable
    T getData();

    Instant getTimestamp();

    @JsonIgnore
    @Override
    default boolean isFailed() {
        return !isSuccess();
    }

    @JsonIgnore
    default boolean isSuccessWithStatus(int status) {
        return isSuccess() && this.getStatus() == status;
    }

    @JsonIgnore
    default boolean isFailedWithStatus(int status) {
        return isFailed() && this.getStatus() == status;
    }

    @Nonnull
    default <D> IResponse<D> cleanData() {
        return transferData(d -> null);
    }

    @Nonnull
    default <D> IResponse<D> transferData(@Nonnull Function<T, D> action) {
        return Response.of(getStatus(), isSuccess(), getCode(), getMessage(),
                action.apply(getData()), getTimestamp());
    }

    @Nonnull
    default <D> IResponse<D> transferDataIfPresent(@Nonnull Function<T, D> action) {
        return transferData(d -> d == null ? null : action.apply(d));
    }
}
