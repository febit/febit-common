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
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.febit.lang.annotation.NonNullArgs;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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

    @Nullable
    String getCode();

    @Nullable
    String getMessage();

    @Nullable
    Instant getTimestamp();

    @Nullable
    T getData();

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
        return map(d -> null);
    }

    /**
     * If a data is present, returns current response, otherwise returns the response produced by the supplying function.
     *
     * @param supplier the supplying function that produces a response to be returned.
     * @since 3.2.1
     */
    @Nonnull
    default IResponse<T> or(@Nonnull Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        }
        return map(d -> supplier.get());
    }

    /**
     * If a data is present, returns the data, otherwise returns {@code other}.
     *
     * @param other the data to be returned, if no data is present.
     * @return the data, if present, otherwise {@code other}
     * @since 3.2.1
     */
    @Nullable
    default T orElse(@Nullable T other) {
        var data = getData();
        return data != null ? data : other;
    }

    /**
     * If a data is present, returns the data, otherwise returns the result produced by the supplying function.
     *
     * @param supplier the supplying function that produces a data to be returned
     * @return the data, if present, otherwise the result produced by the supplying function
     * @throws NullPointerException if no data is present and the supplying function is {@code null}
     * @since 3.2.1
     */
    @Nullable
    default T orElseGet(@Nonnull Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        var data = getData();
        return data != null ? data : supplier.get();
    }

    /**
     * Apply a mapping function to the data.
     *
     * @since 3.2.1
     */
    @Nonnull
    default <D> IResponse<D> map(@Nonnull Function<T, D> mapping) {
        return Response.of(
                getStatus(), isSuccess(),
                getCode(), getMessage(),
                getTimestamp(),
                mapping.apply(getData())
        );
    }

    /**
     * Apply a mapping function to the data if the data is present.
     *
     * @since 3.2.1
     */
    @Nonnull
    default <D> IResponse<D> mapIfPresent(@Nonnull Function<T, D> mapping) {
        return map(d -> d == null ? null : mapping.apply(d));
    }

    /**
     * @deprecated use {@link #map(Function)} instead
     */
    @Nonnull
    @Deprecated(
            since = "3.2.1"
    )
    default <D> IResponse<D> transferData(@Nonnull Function<T, D> mapping) {
        return map(mapping);
    }

    /**
     * @deprecated use {@link #mapIfPresent(Function)} instead
     */
    @Nonnull
    @Deprecated(
            since = "3.2.1"
    )
    default <D> IResponse<D> transferDataIfPresent(@Nonnull Function<T, D> mapping) {
        return mapIfPresent(mapping);
    }

    /**
     * If a data is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a data is present, otherwise {@code false}
     * @since 3.2.1
     */
    @JsonIgnore
    default boolean isPresent() {
        return getData() != null;
    }

    /**
     * If a data is not present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a data is not present, otherwise {@code false}
     * @since 3.2.1
     */
    @JsonIgnore
    default boolean isEmpty() {
        return getData() == null;
    }

}
