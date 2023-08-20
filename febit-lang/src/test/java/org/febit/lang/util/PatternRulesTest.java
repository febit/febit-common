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
package org.febit.lang.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.febit.lang.util.PatternRules.Result;
import static org.febit.lang.util.PatternRules.builder;
import static org.junit.jupiter.api.Assertions.assertNull;

class PatternRulesTest {

    @Test
    void empty() {
        var rules = builder().build();

        assertNull(rules.parse(null));
        assertNull(rules.parse(""));
        assertNull(rules.parse("abc"));
    }

    @Test
    void conflict_same_regex() {
        var builder = builder();
        builder.newRule("A")
                .regex(".*");
        builder.newRule("B")
                .regex(".*");

        var rules = builder.build();
        assertThat(rules.parse(""))
                .isNotNull()
                .returns("A", Result::getRule);
        assertThat(rules.parse("aa"))
                .isNotNull()
                .returns("A", Result::getRule);
        assertThat(rules.parse("bb"))
                .isNotNull()
                .returns("A", Result::getRule);
    }

    @Test
    void conflict_diff_regex() {
        var builder = builder();
        builder.newRule("A")
                .regex("[a-z]+");
        builder.newRule("B")
                .regex("(?i)[a-z]+");

        var rules = builder.build();
        assertThat(rules.parse(""))
                .isNull();
        assertThat(rules.parse("aa"))
                .isNotNull()
                .returns("A", Result::getRule);
        assertThat(rules.parse("aAaA"))
                .isNotNull()
                .returns("B", Result::getRule);
    }

    @Test
    void conflict_text() {
        var builder = builder();
        builder.newRule("A")
                .regex("111");
        builder.newRule("B")
                .regex("111");

        var rules = builder.build();
        assertThat(rules.parse(""))
                .isNull();
        assertThat(rules.parse("111"))
                .isNotNull()
                .returns("A", Result::getRule);
    }

    static class FileLogs {

        @Data
        static class SessionLogin {
            private String account;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class File {
            private String file;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class FileRenamed {
            private String file;
            private String from;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class FileReceived {
            private String file;
            private String speed;
            private Long total;
        }

    }

    private PatternRules fileLogsRules() {
        var builder = builder();

        builder.newRule("SESSION_CLOSE")
                .text("Closed session.");

        builder.newRule("SESSION_LOGIN")
                .text("User '")
                .regex("account", ".*?")
                .text("' logged in")
                .build(FileLogs.SessionLogin.class);

        builder.newRule("FILE_DELETE")
                .text("Deleted file: '")
                .regex("file", ".*?")
                .text("'")
                .build(FileLogs.File.class);

        builder.newRule("FILE_RECEIVING")
                .text("Receiving file: '")
                .regex("file", ".*?")
                .text("'")
                .build(FileLogs.File.class);

        builder.newRule("FILE_RECEIVED")
                .text("Received file: '")
                .regex("file", ".*?")
                .text("' successfully (")
                .regex("speed", ".*?")
                .text(" - ")
                .regex("total", ".*?")
                .text(" Bytes)")
                .build(FileLogs.FileReceived.class);

        builder.newRule("FILE_RENAME")
                .text("Renamed file: '")
                .regex("file", ".*?")
                .text("' from '")
                .regex("from", ".*?")
                .text("'")
                .build(FileLogs.FileRenamed.class);

        builder.newRule("DONE")
                .text("Done!");
        return builder.build();
    }

    @Test
    void fileLogs() {
        var rules = fileLogsRules();

        assertNull(rules.parse(null));
        assertNull(rules.parse("done!"));
        assertNull(rules.parse("Done"));
        assertNull(rules.parse("Done!!"));

        var that = assertThat(rules.parse(""));
        that.isNull();

        assertThat(rules.parse("Done!"))
                .isNotNull()
                .returns("DONE", Result::getRule)
                .returns(Map.of(), Result::getRaw)
                .returns(Map.of(), Result::getBean);

        assertThat(rules.parse("Closed session."))
                .isNotNull()
                .returns("SESSION_CLOSE", Result::getRule)
                .returns(Map.of(), Result::getRaw)
                .returns(Map.of(), Result::getBean);

        // Deleted
        that = assertThat(rules.parse("Deleted file: '/abc/test.txt'"))
                .isNotNull()
                .returns("FILE_DELETE", Result::getRule);
        that.extracting(Result::getRaw, map(String.class, String.class))
                .isNotNull()
                .containsEntry("file", "/abc/test.txt");
        that.extracting(Result::getBean, type(FileLogs.File.class))
                .isNotNull()
                .returns("/abc/test.txt", FileLogs.File::getFile);

        // Renamed
        that = assertThat(rules.parse("Renamed file: 'send.txt' from 'test.txt'"))
                .isNotNull()
                .returns("FILE_RENAME", Result::getRule);
        that.extracting(Result::getRaw, map(String.class, String.class))
                .isNotNull()
                .containsEntry("file", "send.txt")
                .containsEntry("from", "test.txt");
        that.extracting(Result::getBean, type(FileLogs.FileRenamed.class))
                .isNotNull()
                .returns("send.txt", FileLogs.FileRenamed::getFile)
                .returns("test.txt", FileLogs.FileRenamed::getFrom);

        // Receiving
        that = assertThat(rules.parse("Receiving file: 'send.txt'"))
                .isNotNull()
                .returns("FILE_RECEIVING", Result::getRule);
        that.extracting(Result::getRaw, map(String.class, String.class))
                .isNotNull()
                .containsEntry("file", "send.txt");
        that.extracting(Result::getBean, type(FileLogs.File.class))
                .isNotNull()
                .returns("send.txt", FileLogs.File::getFile);

        // Received
        that = assertThat(rules.parse("Received file: 'send.txt' successfully (10 MiB/s - 1000000 Bytes)"))
                .isNotNull()
                .returns("FILE_RECEIVED", Result::getRule);
        that.extracting(Result::getRaw, map(String.class, String.class))
                .isNotNull()
                .containsEntry("file", "send.txt")
                .containsEntry("speed", "10 MiB/s");
        that.extracting(Result::getBean, type(FileLogs.FileReceived.class))
                .isNotNull()
                .returns("send.txt", FileLogs.FileReceived::getFile)
                .returns("10 MiB/s", FileLogs.FileReceived::getSpeed)
                .returns(1000000L, FileLogs.FileReceived::getTotal);
    }
}
