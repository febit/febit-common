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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import jodd.io.FileNameUtil;
import jodd.util.StringPool;
import org.febit.lang.Defaults;
import org.febit.util.Priority;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@Priority.Normal
public class FileResourceLoader implements ResourceLoader {

    protected static final String PREFIX_FILE = "file:";

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        path = formatPath(path);
        if (path == null) {
            return null;
        }
        return new InputStreamReader(new FileInputStream(path), Defaults.or(encoding, StringPool.UTF_8));
    }

    protected String formatPath(String path) {
        if (path == null) {
            return null;
        }
        if (!path.startsWith(PREFIX_FILE)) {
            return null;
        }
        path = StringUtil.cutPrefix(path, PREFIX_FILE);
        path = path.trim();
        path = FileNameUtil.normalize(path);
        return path;
    }

    @Override
    public String normalize(String name) {
        name = formatPath(name);
        if (name == null) {
            return null;
        }
        return PREFIX_FILE + name;
    }
}
