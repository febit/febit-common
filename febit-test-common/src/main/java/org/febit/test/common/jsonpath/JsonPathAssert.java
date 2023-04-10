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
package org.febit.test.common.jsonpath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.Singular;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.internal.Conditions;
import org.assertj.core.util.CheckReturnValue;
import org.febit.lang.util.JacksonUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class JsonPathAssert extends AbstractAssert<JsonPathAssert, Object> {

    private final DocumentContext context;
    Conditions conditions = Conditions.instance();

    protected JsonPathAssert(DocumentContext context) {
        super(context.json(), JsonPathAssert.class);
        this.context = context;
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static JsonPathAssert build0(
            @Nullable String json,
            @Nullable Object parsed,
            @Nullable final JsonProvider jsonProvider,
            @Nullable final MappingProvider mappingProvider,
            @Singular final Set<Option> options,
            @Singular final Collection<EvaluationListener> evaluationListeners
    ) {
        var conf = Configuration.builder()
                .jsonProvider(
                        jsonProvider != null ? jsonProvider : conf().jsonProvider()
                )
                .mappingProvider(
                        mappingProvider != null ? mappingProvider : conf().mappingProvider()
                )
                .options(options)
                .evaluationListener(evaluationListeners)
                .build();
        return parsed != null
                ? assertJsonPath(parsed, conf)
                : assertJsonPath(json, conf);
    }

    public static JsonPathAssert of(DocumentContext context) {
        return new JsonPathAssert(context);
    }

    public static JsonPathAssert assertJsonPath(@Nullable Object parsed) {
        return assertJsonPath(parsed, conf());
    }

    public static JsonPathAssert assertJsonPath(@Nullable String json) {
        return assertJsonPath(json, conf());
    }

    public static JsonPathAssert assertJsonPath(@Nullable Object parsed, Configuration conf) {
        return of(
                JsonPath.parse(parsed, conf)
        );
    }

    public static JsonPathAssert assertJsonPath(@Nullable String json, Configuration conf) {
        if (json == null) {
            return assertJsonPath((Object) null, conf);
        }
        return of(
                JsonPath.parse(json, conf)
        );
    }

    public static Configuration conf() {
        return ConfLazyHolder.CONF;
    }

    public <T> T read(String path) {
        return context.read(path);
    }

    public <T> T read(String path, Class<T> type) {
        return context.read(path, type);
    }

    public <T> T root() {
        return context.json();
    }

    public JsonPathAssert isEqualTo(String path, Object expected) {
        objects.assertEqual(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert isNotEqualTo(String path, Object other) {
        objects.assertNotEqual(info, read(path), other);
        return myself;
    }

    public JsonPathAssert isNull(String path) {
        objects.assertNull(info, read(path));
        return myself;
    }

    public JsonPathAssert isNotNull(String path) {
        objects.assertNotNull(info, read(path));
        return myself;
    }

    public JsonPathAssert isSameAs(String path, Object expected) {
        objects.assertSame(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert isNotSameAs(String path, Object other) {
        objects.assertNotSame(info, read(path), other);
        return myself;
    }

    public JsonPathAssert isInstanceOf(String path, Class<?> type) {
        objects.assertIsInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert isInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert isNotInstanceOf(String path, Class<?> type) {
        objects.assertIsNotInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert isNotInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsNotInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert hasSameClassAs(String path, Object other) {
        objects.assertHasSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert doesNotHaveSameClassAs(String path, Object other) {
        objects.assertDoesNotHaveSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert hasToString(String path, String expectedToString) {
        objects.assertHasToString(info, read(path), expectedToString);
        return myself;
    }

    public JsonPathAssert doesNotHaveToString(String path, String otherToString) {
        objects.assertDoesNotHaveToString(info, read(path), otherToString);
        return myself;
    }

    public JsonPathAssert isExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert isNotExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsNotExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert hasSameHashCodeAs(String path, Object other) {
        objects.assertHasSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert doesNotHaveSameHashCodeAs(String path, Object other) {
        objects.assertDoesNotHaveSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert is(String path, Condition<Object> condition) {
        conditions.assertIs(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert isNot(String path, Condition<Object> condition) {
        conditions.assertIsNot(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert has(String path, Condition<Object> condition) {
        conditions.assertHas(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert doesNotHave(String path, Condition<Object> condition) {
        conditions.assertDoesNotHave(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert satisfies(String path, Condition<Object> condition) {
        conditions.assertSatisfies(info, read(path), condition);
        return myself;
    }

    @CheckReturnValue
    public JsonPathAssert dive(String path) {
        var value = read(path);
        return assertJsonPath(value, context.configuration());
    }

    @CheckReturnValue
    public ListAssert<?> asList(String path) {
        var value = read(path);
        objects.assertIsInstanceOf(info, value, List.class);
        return Assertions.assertThat((List<?>) value);
    }

    @CheckReturnValue
    public MapAssert<?, ?> asMap(String path) {
        var value = read(path);
        objects.assertIsInstanceOf(info, value, Map.class);
        return Assertions.assertThat((Map<?, ?>) value);
    }

    @CheckReturnValue
    public AbstractStringAssert<?> asString(String path) {
        var value = read(path);
        objects.assertIsInstanceOf(info, value, String.class);
        return Assertions.assertThat((String) value);
    }

    @CheckReturnValue
    public ObjectAssert<?> asObject(String path) {
        var value = read(path);
        return Assertions.assertThat(value);
    }

    @SuppressWarnings("unchecked")
    @CheckReturnValue
    public <T, ASSERT extends AbstractAssert<?, ?>> ASSERT as(
            String path, Class<T> type,
            AssertFactory<T, ASSERT> assertFactory
    ) {
        requireNonNull(type);
        requireNonNull(assertFactory);
        var value = read(path);
        objects.assertIsInstanceOf(info, value, type);
        return assertFactory.createAssert((T) value);
    }

    private static class ConfLazyHolder {
        static final Configuration CONF;

        static {
            var mapper = JacksonUtils.standard(new ObjectMapper());
            CONF = Configuration.builder()
                    .jsonProvider(new JacksonJsonProvider(mapper))
                    .mappingProvider(new JacksonMappingProvider(mapper))
                    .build();
        }
    }

}
