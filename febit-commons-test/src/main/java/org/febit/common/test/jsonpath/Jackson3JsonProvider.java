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

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class Jackson3JsonProvider extends AbstractJsonProvider {

    protected final ObjectMapper mapper;

    @Nullable
    @Override
    public Object parse(String json) throws InvalidJsonException {
        try {
            return mapper.readValue(json, Object.class);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e, json);
        }
    }

    @Nullable
    @Override
    public Object parse(InputStream input, String charset) throws InvalidJsonException {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(input, charset);
        } catch (UnsupportedEncodingException e) {
            throw new InvalidJsonException(e);
        }
        try {
            return mapper.readValue(reader, Object.class);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(@Nullable Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public List<Object> createArray() {
        return new LinkedList<>();
    }

    @Override
    public Object createMap() {
        return new LinkedHashMap<String, Object>();
    }
}
