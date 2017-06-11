// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.ServiceLoader;
import jodd.io.StreamUtil;
import org.febit.util.agent.LazyAgent;

/**
 *
 * @author zqq90
 */
public class Resources {

    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final LazyAgent<ResourceLoader[]> LOADERS = new LazyAgent<ResourceLoader[]>() {
        @Override
        protected ResourceLoader[] create() {
            List<ResourceLoader> providerList
                    = CollectionUtil.read(ServiceLoader.load(ResourceLoader.class));
            ResourceLoader[] providers
                    = providerList.toArray(new ResourceLoader[providerList.size()]);
            PriorityUtil.desc(providers);
            return providers;
        }
    };

    public static Reader open(String path) throws IOException {
        return open(path, DEFAULT_ENCODING);
    }

    public static Reader open(String path, String encoding) throws IOException {
        ResourceLoader[] loaders = LOADERS.get();
        Reader reader;
        for (ResourceLoader loader : loaders) {
            reader = loader.openReader(path, encoding);
            if (reader != null) {
                return reader;
            }
        }
        throw new IOException("Resource not found: " + path);
    }

    public static char[] readChars(String path) throws IOException {
        Reader reader = open(path);
        return readChars(reader);
    }

    public static char[] readChars(String path, String encoding) throws IOException {
        Reader reader = open(path, encoding);
        return readChars(reader);
    }

    public static char[] readChars(Reader reader) throws IOException {
        try {
            return StreamUtil.readChars(reader);
        } finally {
            reader.close();
        }
    }

    public static String readString(String path) throws IOException {
        Reader reader = open(path);
        return readString(reader);
    }

    public static String readString(String path, String encoding) throws IOException {
        Reader reader = open(path, encoding);
        return readString(reader);
    }

    public static String readString(Reader reader) throws IOException {
        return new String(readChars(reader));
    }

    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        String result;
        for (ResourceLoader loader : LOADERS.get()) {
            result = loader.normalize(name);
            if (result != null) {
                return result;
            }
        }
        return name;
    }
}
