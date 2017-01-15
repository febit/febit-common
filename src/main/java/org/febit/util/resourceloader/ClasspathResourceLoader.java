// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.resourceloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import jodd.util.StringPool;
import org.febit.lang.Defaults;
import org.febit.util.ClassUtil;
import org.febit.util.PriorityUtil;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@PriorityUtil.Lowest
public class ClasspathResourceLoader implements ResourceLoader {

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        if (path == null) {
            return null;
        }
        path = StringUtil.cutPrefix(path, "classpath:").trim();
        if (path.isEmpty()) {
            return null;
        }
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        final InputStream in = ClassUtil.getDefaultClassLoader()
                .getResourceAsStream(path);
        if (in != null) {
            return new InputStreamReader(in, Defaults.or(encoding, StringPool.UTF_8));
        } else {
            throw new IOException("Resource Not Found: ".concat(path));
        }
    }
}
