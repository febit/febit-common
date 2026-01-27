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
package org.febit.common.rest.client;

import lombok.experimental.UtilityClass;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.JacksonUtils;
import org.springframework.core.ParameterizedTypeReference;

@UtilityClass
public class TypeRefs {

    public static <T> ParameterizedTypeReference<IResponse<T>> forResponse(Class<T> clazz) {
        var type = JacksonUtils.TYPES.constructParametricType(IResponse.class, clazz);
        return ParameterizedTypeReference.forType(type);
    }
}
