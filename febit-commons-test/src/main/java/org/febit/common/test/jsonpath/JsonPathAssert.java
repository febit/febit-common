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
import com.jayway.jsonpath.spi.json.Jackson3JsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
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

public class JsonPathAssert<A> extends AbstractAssert<JsonPathAssert<A>, A> {

    private final Conditions conditions = Conditions.instance();
    private final Configuration conf;

    protected JsonPathAssert(A root, Configuration conf) {
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
    public A root() {
        return actual;
    }

    public JsonPathAssert<A> isEqualTo(String path, Object expected) {
        objects.assertEqual(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert<A> isNotEqualTo(String path, Object other) {
        objects.assertNotEqual(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> isNull(String path) {
        objects.assertNull(info, read(path));
        return myself;
    }

    public JsonPathAssert<A> isNotNull(String path) {
        objects.assertNotNull(info, read(path));
        return myself;
    }

    public JsonPathAssert<A> isSameAs(String path, Object expected) {
        objects.assertSame(info, read(path), expected);
        return myself;
    }

    public JsonPathAssert<A> isNotSameAs(String path, Object other) {
        objects.assertNotSame(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> isInstanceOf(String path, Class<?> type) {
        objects.assertIsInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<A> isInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert<A> isNotInstanceOf(String path, Class<?> type) {
        objects.assertIsNotInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<A> isNotInstanceOfAny(String path, Class<?>... types) {
        objects.assertIsNotInstanceOfAny(info, read(path), types);
        return myself;
    }

    public JsonPathAssert<A> hasSameClassAs(String path, Object other) {
        objects.assertHasSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> doesNotHaveSameClassAs(String path, Object other) {
        objects.assertDoesNotHaveSameClassAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> hasToString(String path, String expectedToString) {
        objects.assertHasToString(info, read(path), expectedToString);
        return myself;
    }

    public JsonPathAssert<A> doesNotHaveToString(String path, String otherToString) {
        objects.assertDoesNotHaveToString(info, read(path), otherToString);
        return myself;
    }

    public JsonPathAssert<A> isExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<A> isNotExactlyInstanceOf(String path, Class<?> type) {
        objects.assertIsNotExactlyInstanceOf(info, read(path), type);
        return myself;
    }

    public JsonPathAssert<A> hasSameHashCodeAs(String path, Object other) {
        objects.assertHasSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> doesNotHaveSameHashCodeAs(String path, Object other) {
        objects.assertDoesNotHaveSameHashCodeAs(info, read(path), other);
        return myself;
    }

    public JsonPathAssert<A> is(String path, Condition<Object> condition) {
        conditions.assertIs(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<A> isNot(String path, Condition<Object> condition) {
        conditions.assertIsNot(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<A> has(String path, Condition<Object> condition) {
        conditions.assertHas(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<A> doesNotHave(String path, Condition<Object> condition) {
        conditions.assertDoesNotHave(info, read(path), condition);
        return myself;
    }

    public JsonPathAssert<A> satisfies(String path, Condition<Object> condition) {
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
