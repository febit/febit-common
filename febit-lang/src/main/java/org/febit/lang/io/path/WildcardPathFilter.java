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
package org.febit.lang.io.path;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WildcardPathFilter implements PathFilter, Predicate<Path>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    final String baseDir;
    final String pattern;
    final boolean sensitive;

    public static WildcardPathFilter create(Path baseDir, String pattern) {
        return create(baseDir, pattern, true);
    }

    public static WildcardPathFilter create(Path baseDir, String pattern, boolean sensitive) {
        var abs = absolutePath(baseDir);
        if (abs == null) {
            throw new IllegalArgumentException("Invalid base dir");
        }
        if (!abs.endsWith("/")) {
            abs += '/';
        }
        if (pattern.isEmpty()) {
            pattern = "*";
        }
        if (pattern.charAt(0) == '/') {
            pattern = pattern.substring(1);
        }
        return new WildcardPathFilter(abs, pattern, sensitive);
    }

    @Override
    public FileVisitResult accept(@Nullable Path path, @Nullable BasicFileAttributes attributes) {
        return test(path)
                ? FileVisitResult.CONTINUE
                : FileVisitResult.TERMINATE;
    }

    @Override
    public boolean test(Path path) {
        if (path == null) {
            return false;
        }
        var abs = absolutePath(path);
        if (abs == null) {
            return false;
        }
        if (!abs.startsWith(baseDir)) {
            return false;
        }
        var rel = abs.substring(baseDir.length());
        return FilenameUtils.wildcardMatch(rel, pattern,
                sensitive ? IOCase.SENSITIVE : IOCase.INSENSITIVE
        );
    }

    static String absolutePath(Path file) {
        var abs = file.toAbsolutePath().normalize().toString();
        return StringUtils.replaceChars(abs, '\\', '/');
    }
}
