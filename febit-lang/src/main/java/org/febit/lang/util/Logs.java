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
package org.febit.lang.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@UtilityClass
public class Logs {

    static final String FAIL_MSG = "!!SERIALIZE_FAILED!!";

    public static <T> Object lazy(Supplier<T> supplier) {
        return new ToStringWrapper<>(supplier);
    }

    public static Object json(@Nullable Object obj) {
        return new ToJsonWrapper<>(() -> obj);
    }

    public static <T> Object lazyJson(Supplier<T> supplier) {
        return new ToJsonWrapper<>(supplier);
    }

    @RequiredArgsConstructor
    private static class ToJsonWrapper<T> {
        private final Supplier<T> supplier;

        @Override
        public String toString() {
            try {
                return JacksonUtils.toJsonString(supplier.get());
            } catch (Exception ex) {
                return FAIL_MSG;
            }
        }
    }

    @RequiredArgsConstructor
    private static class ToStringWrapper<T> {
        private final Supplier<T> supplier;

        @Override
        public String toString() {
            try {
                return String.valueOf(supplier.get());
            } catch (Exception ex) {
                return FAIL_MSG;
            }
        }
    }

}
