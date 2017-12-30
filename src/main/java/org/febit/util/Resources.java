/**
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
package org.febit.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ServiceLoader;
import jodd.io.StreamUtil;

/**
 *
 * @author zqq90
 */
public class Resources {

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static class ResourceLoaderHolder {

        static final ResourceLoader[] LOADERS;

        static {
            LOADERS = CollectionUtil.read(ServiceLoader.load(ResourceLoader.class))
                    .stream()
                    .sorted(Priority.DESC)
                    .toArray(ResourceLoader[]::new);
        }
    }

    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        String result;
        for (ResourceLoader loader : ResourceLoaderHolder.LOADERS) {
            result = loader.normalize(name);
            if (result != null) {
                return result;
            }
        }
        return name;
    }

    public static Reader open(String path, String encoding) throws IOException {
        for (ResourceLoader loader : ResourceLoaderHolder.LOADERS) {
            Reader reader = loader.openReader(path, encoding);
            if (reader != null) {
                return reader;
            }
        }
        throw new IOException("Resource not found: " + path);
    }

    public static Reader open(String path) throws IOException {
        return open(path, DEFAULT_ENCODING);
    }

    public static char[] readChars(String path, String encoding) throws IOException {
        try (Reader reader = open(path, encoding)) {
            return StreamUtil.readChars(reader);
        }
    }

    public static char[] readChars(String path) throws IOException {
        return readChars(path, DEFAULT_ENCODING);
    }

    public static String readString(String path) throws IOException {
        return new String(readChars(path));
    }

    public static String readString(String path, String encoding) throws IOException {
        return new String(readChars(path, encoding));
    }
}
