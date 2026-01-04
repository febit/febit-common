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

import org.jspecify.annotations.Nullable;

public interface IBasicApi {

    default <T extends @Nullable Object> IResponse<T> ok() {
        return IResponse.ok();
    }

    default <T extends @Nullable Object> IResponse<T> ok(T data) {
        return IResponse.ok(data);
    }

    default <T extends @Nullable Object> IResponse<T> created() {
        return IResponse.ok(201, null);
    }

    default <T extends @Nullable Object> IResponse<T> accepted() {
        return IResponse.ok(202, null);
    }
}
