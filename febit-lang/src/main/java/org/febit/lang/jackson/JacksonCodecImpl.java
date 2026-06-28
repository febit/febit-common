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
package org.febit.lang.jackson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;

import java.io.Serial;
import java.util.function.UnaryOperator;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "of")
public class JacksonCodecImpl<M extends ObjectMapper> implements JacksonCodec {

    @Serial
    private static final long serialVersionUID = 2L;

    @Getter
    private final M mapper;

    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> JacksonCodecImpl<M> ofStandard(B builder) {
        var mapper = JacksonStandard.standard(builder)
                .build();
        return of(mapper);
    }

    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> JacksonCodecImpl<M> ofStandard(
            B builder, UnaryOperator<B> customizer) {
        var mapper = customizer
                .apply(JacksonStandard.standard(builder))
                .build();
        return of(mapper);
    }
}
