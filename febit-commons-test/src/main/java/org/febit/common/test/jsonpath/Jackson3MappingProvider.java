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
package org.febit.common.test.jsonpath;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class Jackson3MappingProvider implements MappingProvider {

    private final ObjectMapper mapper;

    @Nullable
    @Override
    public <T> T map(@Nullable Object source, Class<T> targetType, Configuration conf) {
        if (source == null) {
            return null;
        }
        try {
            return mapper.convertValue(source, targetType);
        } catch (Exception e) {
            throw new MappingException(e);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T map(@Nullable Object source, final TypeRef<T> targetType, Configuration conf) {
        if (source == null) {
            return null;
        }
        var type = mapper.getTypeFactory().constructType(targetType.getType());
        try {
            return (T) mapper.convertValue(source, type);
        } catch (Exception e) {
            throw new MappingException(e);
        }
    }
}
