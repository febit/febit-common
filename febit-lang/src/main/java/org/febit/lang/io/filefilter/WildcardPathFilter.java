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
package org.febit.lang.io.filefilter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WildcardPathFilter implements IOFileFilter, Serializable {

    private static final long serialVersionUID = 1L;

    final String baseDir;
    final String pattern;
    final boolean sensitive;

    public static WildcardPathFilter create(File baseDir, String pattern) {
        return create(baseDir, pattern, true);
    }

    public static WildcardPathFilter create(File baseDir, String pattern, boolean sensitive) {
        var abs = getAbsolutePath(baseDir);
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
    public boolean accept(final File file) {
        var abs = getAbsolutePath(file);
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

    @Override
    public boolean accept(final File dir, final String name) {
        if (name.isEmpty()) {
            return accept(dir);
        }
        if (name.startsWith("/")) {
            return accept(new File(FilenameUtils.normalize(name)));
        }
        return accept(new File(dir, name));
    }

    @Nullable
    static String getAbsolutePath(File file) {
        return FilenameUtils.normalize(file.getAbsolutePath(), true);
    }
}
