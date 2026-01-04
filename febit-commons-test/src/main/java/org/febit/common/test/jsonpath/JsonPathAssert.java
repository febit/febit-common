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
import com.jayway.jsonpath.EvaluationListener;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.Singular;
import org.assertj.core.annotation.CheckReturnValue;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.internal.Conditions;
import org.febit.lang.util.JacksonUtils;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class JsonPathAssert<T> extends AbstractAssert<JsonPathAssert<T>, T> {

    private final Conditions conditions = Conditions.instance();
    private final Configuration conf;

    protected JsonPathAssert(T root, Configuration conf) {
        super(root, JsonPathAssert.class);
        this.conf = conf;
    }

    private JsonPath compileIfAbsent(String pattern) {
        var cache = CacheProvider.getCache();
        var path = cache.get(pattern);
        if (path == null) {
            path = JsonPath.compile(pattern);
            cache.put(pattern, path);
        }
        return path;
    }

    public static <T> JsonPathAssert.Builder<T> builder() {
        return new JsonPathAssert.Builder<>();
    }

    public static <T> JsonPathAssert.Builder<T> builder(String json) {
        return new JsonPathAssert.Builder<T>()
                .json(json);
    }

    public static <T> JsonPathAssert.Builder<T> builder(T parsed) {
        return new JsonPathAssert.Builder<T>()
                .parsed(parsed);
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static <T> JsonPathAssert<T> create(
            @Nullable String json,
            @Nullable T parsed,
            @Nullable final JsonProvider jsonProvider,
            @Nullable final MappingProvider mappingProvider,
            @Singular final Set<Option> options,
            @Singular final Collection<EvaluationListener> evaluationListeners
    ) {
        var conf = Configuration.builder()
                .jsonProvider(
                        jsonProvider != null ? jsonProvider : defaultConf().jsonProvider()
                )
                .mappingProvider(
                        mappingProvider != null ? mappingProvider : defaultConf().mappingProvider()
                )
                .options(options)
                .options(Option.SUPPRESS_EXCEPTIONS)
                .evaluationListener(evaluationListeners)
                .build();

        if (parsed == null && json != null) {
            return assertJsonPath(json, conf);
        }
        return assertJsonPath(parsed, conf);
    }

    public static <T> JsonPathAssert<T> of(T obj, Configuration conf) {
        return new JsonPathAssert<>(obj, conf);
    }

    public static <T> JsonPathAssert<T> assertJsonPath(@Nullable T parsed) {
        return assertJsonPath(parsed, defaultConf());
    }

    public static <T> JsonPathAssert<T> assertJsonPath(@Nullable String json) {
        return assertJsonPath(json, defaultConf());
    }

    public static <T> JsonPathAssert<T> assertJsonPath(@Nullable T parsed, Configuration conf) {
        return of(parsed, conf);
    }

    public static <T> JsonPathAssert<T> assertJsonPath(@Nullable String json, Configuration conf) {
        if (json == null) {
            return assertJsonPath((T) null, conf);
        }
        @SuppressWarnings("unchecked")
        var parsed = (T) conf.jsonProvider().parse(json);
        return of(parsed, conf);
    }

    public static Configuration defaultConf() {
        return ConfLazyHolder.CONF;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <R> R read(String pattern) {
        var path = compileIfAbsent(pattern);
        var root = root();
        if (root == null) {
            return path.isDefinite() ? null : (R) List.of();
        }
        return path.read(root, conf);
    }

    @Nullable
    public <R> R read(String pattern, Class<R> type) {
        var path = compileIfAbsent(pattern);
        var result = path.read(root(), conf);
        return conf.mappingProvider().map(result, type, conf);
    }

    @Nullable
    public T root() {
        return actual;
    }

    public JsonPathAssert<T> isEqualTo(String path, Object expected) {
        objects.assertEqual(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert<T> isNotEqualTo(String path, Object other) {
        objects.assertNotEqual(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> isNull(String path) {
        objects.assertNull(info, read(path));
        return myself;
    }

    public JsonPathAssert<T> isNotNull(String path) {
        objects.assertNotNull(info, read(path));
        return myself;
    }

    public JsonPathAssert<T> isSameAs(String path, Object expected) {
        objects.assertSame(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert<T> isNotSameAs(String path, Object other) {
        objects.assertNotSame(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> isInstanceOf(String path, Class<?> type) {
        objects.assertIsInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<T> isInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert<T> isNotInstanceOf(String path, Class<?> type) {
        objects.assertIsNotInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<T> isNotInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsNotInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert<T> hasSameClassAs(String path, Object other) {
        objects.assertHasSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> doesNotHaveSameClassAs(String path, Object other) {
        objects.assertDoesNotHaveSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> hasToString(String path, String expectedToString) {
        objects.assertHasToString(info, read(path), expectedToString);
        return myself;
    }

    public JsonPathAssert<T> doesNotHaveToString(String path, String otherToString) {
        objects.assertDoesNotHaveToString(info, read(path), otherToString);
        return myself;
    }

    public JsonPathAssert<T> isExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<T> isNotExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsNotExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<T> hasSameHashCodeAs(String path, Object other) {
        objects.assertHasSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> doesNotHaveSameHashCodeAs(String path, Object other) {
        objects.assertDoesNotHaveSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<T> is(String path, Condition<Object> condition) {
        conditions.assertIs(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<T> isNot(String path, Condition<Object> condition) {
        conditions.assertIsNot(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<T> has(String path, Condition<Object> condition) {
        conditions.assertHas(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<T> doesNotHave(String path, Condition<Object> condition) {
        conditions.assertDoesNotHave(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<T> satisfies(String path, Condition<Object> condition) {
        conditions.assertSatisfies(info, read(path), condition);
        return myself;
    }

    @CheckReturnValue
    public <R> JsonPathAssert<R> dive(String path) {
        @SuppressWarnings("unchecked")
        var value = (R) read(path);
        return assertJsonPath(value, conf);
    }

    @CheckReturnValue
    public <R> ListAssert<R> asList(String path) {
        @SuppressWarnings("unchecked")
        var value = (List<R>) read(path);
        objects.assertIsInstanceOf(info, value, List.class);
        return Assertions.assertThat(value);
    }

    @CheckReturnValue
    public <K, V> MapAssert<K, V> asMap(String path) {
        @SuppressWarnings("unchecked")
        var value = (Map<K, V>) read(path);
        objects.assertIsInstanceOf(info, value, Map.class);
        return Assertions.assertThat(value);
    }

    @CheckReturnValue
    public AbstractStringAssert<?> asString(String path) {
        var value = (String) read(path);
        objects.assertIsInstanceOf(info, value, String.class);
        return Assertions.assertThat(value);
    }

    @CheckReturnValue
    public ObjectAssert<?> asObject(String path) {
        var value = read(path);
        return Assertions.assertThat(value);
    }

    @CheckReturnValue
    public <R, ASSERT extends AbstractAssert<?, ?>> ASSERT as(
            String path, Class<R> type,
            AssertFactory<R, ASSERT> assertFactory
    ) {
        requireNonNull(type);
        requireNonNull(assertFactory);
        var value = read(path, type);
        objects.assertIsInstanceOf(info, value, type);
        return assertFactory.createAssert(value);
    }

    private static class ConfLazyHolder {
        static final Configuration CONF;

        static {
            var mapper = JacksonUtils.standard(JsonMapper.builder())
                    .build();
            CONF = Configuration.builder()
                    .jsonProvider(new Jackson3JsonProvider(mapper))
                    .mappingProvider(new Jackson3MappingProvider(mapper))
                    .options(Option.SUPPRESS_EXCEPTIONS)
                    .build();
        }
    }

}
