// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.resourceloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import jodd.io.FileNameUtil;
import jodd.util.StringPool;
import org.febit.lang.Defaults;
import org.febit.util.PriorityUtil;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@PriorityUtil.Normal
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
