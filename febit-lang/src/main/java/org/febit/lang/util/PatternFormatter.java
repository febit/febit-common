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

import com.fasterxml.jackson.databind.JavaType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;
import static org.febit.lang.util.JacksonWrapper.TYPE_MAP_NAMED;

@RequiredArgsConstructor(
        access = AccessLevel.PRIVATE,
        staticName = "create"
)
public class PatternFormatter<T> implements Serializable {

    @Getter
    @Nonnull
    private final JavaType resultType;

    @Nonnull
    private final List<Segment> segments;

    /**
     * Mapping segment key to var name.
     */
    @Getter
    @Nonnull
    private final Map<String, String> varNameMapping;

    @Getter
    @Nonnull
    private final Pattern pattern;

    public static Builder builder() {
        return new Builder("");
    }

    /**
     * Format by given vars.
     *
     * @param vars variables
     * @return formatted text
     */
    public String format(VarResolver vars) {
        var buf = new StringBuilder();
        segments.forEach(segment -> segment.emit(buf, vars));
        return buf.toString();
    }

    /**
     * Format by given vars.
     *
     * @param vars variables
     * @return formatted text
     */
    public String format(@Nullable Map<String, ?> vars) {
        return format(
                vars != null ? vars::get
                        : VarResolver.ofEmpty()
        );
    }

    /**
     * Format string by given data.
     * <p>
     * Will convert bean to map first.
     *
     * @param bean source
     * @return formatted string
     * @see JacksonUtils#toNamedMap(Object)
     */
    public String format(@Nullable T bean) {
        var vars = JacksonUtils.toNamedMap(bean);
        return format(vars);
    }

    /**
     * Parse given text to bean.
     *
     * @param text input text
     * @return null if not match.
     */
    @Nullable
    public T parse(@Nullable String text) {
        if (text == null) {
            return null;
        }
        var raw = parseRaw(text);
        return JacksonUtils.to(raw, resultType);
    }

    /**
     * Parse given text to raw map.
     *
     * @param text input text
     * @return null if not match.
     */
    @Nullable
    public Map<String, String> parseRaw(@Nullable String text) {
        if (text == null) {
            return null;
        }
        var matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return resolveRaw(matcher);
    }

    protected Map<String, String> resolveRaw(Matcher matcher) {
        return Maps.mapping(
                varNameMapping.entrySet(),
                Map.Entry::getValue,
                e -> matcher.group(e.getKey())
        );
    }

    public static class Builder {

        private final List<Segment> segments = new ArrayList<>();

        private final String keyPrefix;

        protected Builder(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public Builder text(String text) {
            segments.add(new TextSegment(text));
            return this;
        }

        public Builder regex(String pattern) {
            return regex(null, pattern);
        }

        public Builder regex(@Nullable String name, String pattern) {
            segments.add(new RegexSegment(keyPrefix, segments.size() + 1, name, pattern));
            return this;
        }

        public PatternFormatter<Map<String, Object>> build() {
            return build(TYPE_MAP_NAMED);
        }

        public <T> PatternFormatter<T> build(Class<? extends T> beanType) {
            return build(
                    TYPE_FACTORY.constructType(beanType)
            );
        }

        protected <T> PatternFormatter<T> build(JavaType beanType) {
            var pattern = segments.stream()
                    .map(Segment::toRegex)
                    .collect(Collectors.joining());
            var mapping = Map.<String, String>ofEntries(
                    segments.stream()
                            .map(Segment::keyNamePair)
                            .filter(Objects::nonNull)
                            .toArray(Pairs::newArray)
            );
            return PatternFormatter.create(
                    beanType, segments, mapping,
                    Pattern.compile("^(" + pattern + ")$")
            );
        }
    }

    public interface VarResolver {

        @Nullable
        Object get(@Nonnull String name);

        static VarResolver ofEmpty() {
            return name -> null;
        }
    }

    private interface Segment extends Serializable {

        void emit(StringBuilder buf, VarResolver vars);

        String toRegex();

        @Nullable
        default String name() {
            return null;
        }

        @Nullable
        default String segmentKey() {
            return null;
        }

        @Nullable
        default Pair<String, String> keyNamePair() {
            var key = segmentKey();
            var name = name();
            if (key == null || name == null) {
                return null;
            }
            return Pair.of(key, name);
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class RegexSegment implements Segment {

        private final String keyPrefix;
        private final int seq;

        @Nullable
        private final String name;
        private final String pattern;

        @Override
        public void emit(StringBuilder buf, VarResolver resolver) {
            if (name == null) {
                return;
            }
            var value = resolver.get(name);
            if (value != null) {
                buf.append(value);
            }
        }

        @Override
        public String toRegex() {
            return "(?<" + segmentKey() + ">" + pattern + ")";
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String segmentKey() {
            return keyPrefix + "s" + seq;
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class TextSegment implements Segment {

        private final String value;

        @Override
        public void emit(StringBuilder buf, VarResolver resolver) {
            buf.append(value);
        }

        @Override
        public String toRegex() {
            return PatternUtils.escape(value);
        }
    }
}
