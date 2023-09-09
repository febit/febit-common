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
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor(
        staticName = "of",
        access = AccessLevel.PACKAGE
)
public class PatternRules implements Serializable {

    private final Pattern pattern;
    private final Map<String, Rule> rules;

    /**
     * Parse given text.
     *
     * @param text input text
     * @return null if not match.
     */
    @Nullable
    public Result parse(@Nullable String text) {
        if (text == null) {
            return null;
        }
        var matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        var rule = rules.entrySet().stream()
                .filter(e -> matcher.group(e.getKey()) != null)
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (rule == null) {
            return null;
        }
        var formatter = rule.formatter;

        var raw = formatter.resolveRaw(matcher);
        var bean = JacksonUtils.to(raw, formatter.getResultType());

        return Result.of(rule.name, raw, bean);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    public static class Result {

        private final String rule;
        private final Map<String, String> raw;
        private final Object bean;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Rule implements Comparable<Rule>, Serializable {
        private final int seq;
        private final String name;
        private final PatternFormatter<?> formatter;

        @Override
        public int compareTo(Rule o) {
            return Integer.compare(seq, o.seq);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder {

        private final List<Rule> rules = new ArrayList<>();

        private PatternFormatter.Builder pending;

        protected void pushPendingIfPresent() {
            if (pending == null) {
                return;
            }
            pending.build();
        }

        public PatternRules build() {
            pushPendingIfPresent();

            var pairs = rules.stream()
                    .map(r -> Pair.of("r" + r.seq, r))
                    .toArray(Pairs::<String, Rule>newArray);

            var regex = new StringBuilder();
            for (var pair : pairs) {
                var rule = pair.getValue();
                if (rule.seq != 1) {
                    regex.append("|");
                }
                regex.append("(?<")
                        .append(pair.getKey())
                        .append(">")
                        .append(rule.formatter.getPattern().pattern())
                        .append(")");
            }
            return of(
                    Pattern.compile(regex.toString()),
                    Map.ofEntries(pairs)
            );
        }

        public PatternFormatter.Builder newRule(String name) {
            pushPendingIfPresent();

            var seq = rules.size() + 1;
            pending = new PatternFormatter.Builder("r" + seq) {

                @Override
                protected <T> PatternFormatter<T> build(JavaType beanType) {
                    var formatter = super.<T>build(beanType);
                    pending = null;
                    rules.add(Rule.of(seq, name, formatter));
                    return formatter;
                }
            };
            return pending;
        }
    }
}
