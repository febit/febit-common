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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.UtilityClass;
import org.febit.lang.util.TimeUtils;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.InstantSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@UtilityClass
public class JacksonStandard {

    public static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B standard(B builder) {
        var module = new SimpleModule()
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(TimeUtils.FMT_TIME))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(TimeUtils.FMT_TIME))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(TimeUtils.FMT_DATE))
                .addSerializer(LocalDate.class, new LocalDateSerializer(TimeUtils.FMT_DATE))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(TimeUtils.FMT_DATE_TIME))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(TimeUtils.FMT_DATE_TIME))
                .addSerializer(Instant.class, InstantSerializer.INSTANCE);

        builder.changeDefaultPropertyInclusion(inclusion -> inclusion
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL)
                )
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .defaultPrettyPrinter(
                        new StandardPrettyPrinter()
                )
                .addModule(module);
        return builder;
    }

}
