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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultProviderImplTest {

    @Test
    void shouldReturnDefaultValueForExistingProperty() {
        var props = new Properties();
        props.setProperty("server.port", "8080");
        var provider = DefaultProviderImpl.of(props);

        assertThat(provider.getDefaultValueFor("server.port")).isEqualTo("8080");
    }

    @Test
    void shouldReturnNullForNonExistingProperty() {
        var props = new Properties();
        var provider = DefaultProviderImpl.of(props);

        assertThat(provider.getDefaultValueFor("nonexistent")).isNull();
    }

    @Test
    void shouldThrowNpeForNullKey() {
        var props = new Properties();
        props.setProperty("key", "value");
        var provider = DefaultProviderImpl.of(props);

        assertThrows(NullPointerException.class, () -> provider.getDefaultValueFor(null));
    }

    @Test
    void shouldHandleEmptyProperties() {
        var provider = DefaultProviderImpl.of(new Properties());

        assertThat(provider.getDefaultValueFor("anything")).isNull();
    }

    @Test
    void fromDirShouldLoadOptionsFiles(@TempDir Path tempDir) throws IOException {
        var file1 = tempDir.resolve("cmd1.options").toFile();
        var file2 = tempDir.resolve("cmd2.options").toFile();
        Files.writeString(file1.toPath(), "server.port=8080\nserver.host=localhost");
        Files.writeString(file2.toPath(), "db.url=jdbc:mysql://localhost/test\ndb.user=root");

        var provider = DefaultProviderImpl.fromDir(tempDir.toFile());

        assertThat(provider.getDefaultValueFor("server.port")).isEqualTo("8080");
        assertThat(provider.getDefaultValueFor("server.host")).isEqualTo("localhost");
        assertThat(provider.getDefaultValueFor("db.url")).isEqualTo("jdbc:mysql://localhost/test");
        assertThat(provider.getDefaultValueFor("db.user")).isEqualTo("root");
    }

    @Test
    void fromDirShouldHandleEmptyDirectory(@TempDir Path tempDir) {
        var provider = DefaultProviderImpl.fromDir(tempDir.toFile());

        assertThat(provider.getDefaultValueFor("anything")).isNull();
    }

    @Test
    void fromDirShouldIgnoreNonOptionFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("readme.txt"), "some text");
        Files.writeString(tempDir.resolve("config.properties"), "key=value");
        var optionFile = tempDir.resolve("myapp.options").toFile();
        Files.writeString(optionFile.toPath(), "myapp.name=test");

        var provider = DefaultProviderImpl.fromDir(tempDir.toFile());

        assertThat(provider.getDefaultValueFor("myapp.name")).isEqualTo("test");
        assertThat(provider.getDefaultValueFor("key")).isNull();
    }

    @Test
    void fromDirShouldOverrideKeysWithLaterFiles(@TempDir Path tempDir) throws IOException {
        var file1 = tempDir.resolve("a.options").toFile();
        var file2 = tempDir.resolve("b.options").toFile();
        Files.writeString(file1.toPath(), "override.key=first");
        Files.writeString(file2.toPath(), "override.key=second");

        var provider = DefaultProviderImpl.fromDir(tempDir.toFile());

        assertThat(provider.getDefaultValueFor("override.key")).isEqualTo("second");
    }

    @Test
    void fromWorkDirShouldNotThrow() {
        assertDoesNotThrow(DefaultProviderImpl::fromWorkDir);
    }

    @Test
    void fromSystemPropsShouldReturnEmptyWhenNoPropertySet() {
        var oldValue = System.getProperty("FEBIT_CMD_DEFAULT_OPTS");
        System.clearProperty("FEBIT_CMD_DEFAULT_OPTS");
        try {
            var provider = DefaultProviderImpl.fromSystemProps();
            assertThat(provider.getDefaultValueFor("anything")).isNull();
        } finally {
            if (oldValue != null) {
                System.setProperty("FEBIT_CMD_DEFAULT_OPTS", oldValue);
            }
        }
    }

    @Test
    void fromSystemPropsShouldReturnEmptyWhenEmptyPropertySet() {
        var oldValue = System.getProperty("FEBIT_CMD_DEFAULT_OPTS");
        System.setProperty("FEBIT_CMD_DEFAULT_OPTS", "");
        try {
            var provider = DefaultProviderImpl.fromSystemProps();
            assertThat(provider.getDefaultValueFor("anything")).isNull();
        } finally {
            if (oldValue != null) {
                System.setProperty("FEBIT_CMD_DEFAULT_OPTS", oldValue);
            } else {
                System.clearProperty("FEBIT_CMD_DEFAULT_OPTS");
            }
        }
    }

    @Test
    void fromSystemPropsShouldThrowForInvalidPath() {
        var oldValue = System.getProperty("FEBIT_CMD_DEFAULT_OPTS");
        System.setProperty("FEBIT_CMD_DEFAULT_OPTS", "file:///nonexistent/path.options");
        try {
            assertThrows(UncheckedIOException.class,
                    DefaultProviderImpl::fromSystemProps);
        } finally {
            if (oldValue != null) {
                System.setProperty("FEBIT_CMD_DEFAULT_OPTS", oldValue);
            } else {
                System.clearProperty("FEBIT_CMD_DEFAULT_OPTS");
            }
        }
    }

    @Test
    void fromSystemPropsShouldLoadFromValidUri(@TempDir Path tempDir) throws IOException {
        var optionFile = tempDir.resolve("test.options").toFile();
        Files.writeString(optionFile.toPath(), "loaded.key=from_file");

        var oldValue = System.getProperty("FEBIT_CMD_DEFAULT_OPTS");
        System.setProperty("FEBIT_CMD_DEFAULT_OPTS", optionFile.toURI().toString());
        try {
            var provider = DefaultProviderImpl.fromSystemProps();
            assertThat(provider.getDefaultValueFor("loaded.key")).isEqualTo("from_file");
        } finally {
            if (oldValue != null) {
                System.setProperty("FEBIT_CMD_DEFAULT_OPTS", oldValue);
            } else {
                System.clearProperty("FEBIT_CMD_DEFAULT_OPTS");
            }
        }
    }

    @Test
    void fromSystemPropsShouldLoadFromMultiplePaths(@TempDir Path tempDir) throws IOException {
        var file1 = tempDir.resolve("opts1.options").toFile();
        var file2 = tempDir.resolve("opts2.options").toFile();
        Files.writeString(file1.toPath(), "key1=val1");
        Files.writeString(file2.toPath(), "key2=val2");

        var oldValue = System.getProperty("FEBIT_CMD_DEFAULT_OPTS");
        System.setProperty("FEBIT_CMD_DEFAULT_OPTS",
                file1.toURI() + "," + file2.toURI());
        try {
            var provider = DefaultProviderImpl.fromSystemProps();
            assertThat(provider.getDefaultValueFor("key1")).isEqualTo("val1");
            assertThat(provider.getDefaultValueFor("key2")).isEqualTo("val2");
        } finally {
            if (oldValue != null) {
                System.setProperty("FEBIT_CMD_DEFAULT_OPTS", oldValue);
            } else {
                System.clearProperty("FEBIT_CMD_DEFAULT_OPTS");
            }
        }
    }
}
