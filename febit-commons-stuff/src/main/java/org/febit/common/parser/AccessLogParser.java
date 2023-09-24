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
package org.febit.common.parser;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.Lists;
import org.febit.lang.util.Pairs;
import org.febit.lang.util.StringWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class AccessLogParser {

    public static List<String> parsePattern(@Nullable String pattern) {
        var raw = parse(pattern);
        return Lists.collect(raw, expr -> {
            if (expr == null || !expr.startsWith("$")) {
                return null;
            }
            return expr.substring(1);
        });
    }

    public static <T> T parseToBean(Class<T> type, @Nullable String text, List<String> keys) {
        var src = parseToMap(text, keys);
        var bean = JacksonUtils.to(src, type);
        Objects.requireNonNull(bean);
        return bean;
    }

    public static Map<String, String> parseToMap(@Nullable String text, List<String> keys) {
        var values = parse(text);
        var size = Math.min(keys.size(), values.size());
        if (size == 0) {
            return Map.of();
        }

        var pairs = Pairs.<String, String>newArray(size);
        for (int i = 0; i < size; i++) {
            pairs[i] = Pair.of(keys.get(i), values.get(i));
        }
        return Map.ofEntries(pairs);
    }

    @Nullable
    private static String fixValue(@Nullable String value) {
        if (value == null
                || value.equals("-")) {
            return null;
        }
        return value;
    }

    public static List<String> parse(@Nullable String text) {
        if (StringUtils.isBlank(text)) {
            return List.of();
        }

        var values = new ArrayList<String>();
        var walker = new StringWalker(text);

        walker.skipSpaces();
        while (!walker.isEnd()) {
            switch (walker.peek()) {
                case '[':
                    walker.jump(1);
                    values.add(fixValue(walker.readTo(']', false)));
                    break;
                case '"':
                    walker.jump(1);
                    values.add(fixValue(walker.readTo('"', false)));
                    break;
                default:
                    values.add(fixValue(walker.readTo(' ', false)));
            }
            walker.skipSpaces();
        }
        return values;
    }
}
