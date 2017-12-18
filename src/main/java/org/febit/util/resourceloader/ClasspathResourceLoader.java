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
package org.febit.util.resourceloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import jodd.io.FileNameUtil;
import jodd.util.StringPool;
import org.febit.lang.Defaults;
import org.febit.util.ClassUtil;
import org.febit.util.Priority;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@Priority.Lowest
public class ClasspathResourceLoader implements ResourceLoader {

    protected static final String PREFIX_CLASSPATH = "classpath:";

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        path = formatPath(path);
        if (path == null) {
            return null;
        }
        final InputStream in = ClassUtil.getDefaultClassLoader()
                .getResourceAsStream(path);
        if (in != null) {
            return new InputStreamReader(in, Defaults.or(encoding, StringPool.UTF_8));
        } else {
            throw new IOException("Resource Not Found: ".concat(path));
        }
    }

    protected String formatPath(String path) {
        if (path == null) {
            return null;
        }
        path = StringUtil.cutPrefix(path, PREFIX_CLASSPATH);
        path = path.trim();
        if (path.isEmpty()) {
            return null;
        }
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        path = FileNameUtil.normalize(path, true);
        return path;
    }

    @Override
    public String normalize(String name) {
        name = formatPath(name);
        if (name == null) {
            return null;
        }
        return PREFIX_CLASSPATH + name;
    }
}
