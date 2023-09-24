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
package org.febit.common.jcommander;

import com.beust.jcommander.IDefaultProvider;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class DefaultProviderImpl implements IDefaultProvider {

    private static final String DEFAULT_OPTS = "FEBIT_CMD_DEFAULT_OPTS";
    private static final String EXTENSION = ".options";

    private final Properties props;

    @Nullable
    @Override
    public String getDefaultValueFor(String option) {
        return props.getProperty(option);
    }

    public static DefaultProviderImpl fromWorkDir() {
        var dir = new File("").getAbsoluteFile();
        return fromDir(dir);
    }

    public static DefaultProviderImpl fromSystemProps() {
        var props = new Properties();

        var paths = StringUtils.split(System.getProperty(DEFAULT_OPTS), ',');
        if (paths == null || paths.length == 0) {
            log.info("Not path set in system props: {}", DEFAULT_OPTS);
            return of(props);
        }

        for (var p : paths) {
            p = p.trim();
            try (
                    var in = new InputStreamReader(new URL(p).openStream(), StandardCharsets.UTF_8)
            ) {
                log.info("Loading options from path: {}", p);
                props.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot load options from path: " + p, e);
            }
        }

        return of(props);
    }

    public static DefaultProviderImpl fromDir(File parent) {
        var props = new Properties();
        var abs = parent.getAbsoluteFile();

        log.info("Scanning options files under: {}", abs);
        var files = parent.listFiles((dir, name) -> name.endsWith(EXTENSION));
        if (files == null || files.length == 0) {
            log.info("No options file found under: {}", abs);
            return of(props);
        }

        for (var file : files) {
            try (
                    var in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            ) {
                log.info("Loading options from file: {}", file.getAbsolutePath());
                props.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot load options from file: " + file.getAbsolutePath(), e);
            }
        }

        return of(props);
    }
}
