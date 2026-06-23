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
package org.febit.common.parser;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLogParserTest {

    @Data
    public static class AccessLogBean {
        private String ip;
        private String user;
        private String time;
        private String request;
        private Integer status;
        private Integer size;
    }

    @Test
    void shouldParseSimpleSpaceDelimitedValues() {
        var result = AccessLogParser.parse("192.168.1.1 - - [10/Oct/2023:13:55:36] GET /index.html 200 2326");

        assertThat(result).containsExactly(
                "192.168.1.1", null, null,
                "10/Oct/2023:13:55:36",
                "GET",
                "/index.html",
                "200",
                "2326"
        );
    }

    @Test
    void shouldParseQuotedValues() {
        var result = AccessLogParser.parse("\"Mozilla/5.0\" \"-\"");

        assertThat(result).containsExactly("Mozilla/5.0", null);
    }

    @Test
    void shouldParseBracketedValues() {
        var result = AccessLogParser.parse("[10/Oct/2023:13:55:36 +0000]");

        assertThat(result).containsExactly("10/Oct/2023:13:55:36 +0000");
    }

    @Test
    void shouldParseMixedFormat() {
        var result = AccessLogParser.parse("192.168.1.1 - [10/Oct/2023:13:55:36] \"GET /api\" 200");

        assertThat(result).containsExactly(
                "192.168.1.1", null,
                "10/Oct/2023:13:55:36",
                "GET /api",
                "200"
        );
    }

    @Test
    void shouldReturnEmptyListForBlankInput() {
        assertThat(AccessLogParser.parse(null)).isEmpty();
        assertThat(AccessLogParser.parse("")).isEmpty();
        assertThat(AccessLogParser.parse("   ")).isEmpty();
    }

    @Test
    void shouldConvertDashToNull() {
        var result = AccessLogParser.parse("- - normal");
        assertThat(result).containsExactly(null, null, "normal");
    }

    @Test
    void shouldParsePatternExtractingDollarPrefixedNames() {
        var result = AccessLogParser.parsePattern("$host $user [$time] \"$request\" $status");

        assertThat(result).containsExactly("host", "user", "time", "request", "status");
    }

    @Test
    void shouldSkipNonDollarPrefixedInParsePattern() {
        var result = AccessLogParser.parsePattern("static $dynamic");

        assertThat(result).containsExactly(null, "dynamic");
    }

    @Test
    void shouldParsePatternReturnEmptyForBlankInput() {
        assertThat(AccessLogParser.parsePattern(null)).isEmpty();
        assertThat(AccessLogParser.parsePattern("")).isEmpty();
    }

    @Test
    void shouldParseToMapWithNonDashValues() {
        var keys = List.of("ip", "time", "request", "status", "size");
        var text = "192.168.1.1 [10/Oct/2023:13:55:36] \"GET /api\" 200 2326";

        var result = AccessLogParser.parseToMap(text, keys);

        assertThat(result)
                .containsEntry("ip", "192.168.1.1")
                .containsEntry("time", "10/Oct/2023:13:55:36")
                .containsEntry("request", "GET /api")
                .containsEntry("status", "200")
                .containsEntry("size", "2326");
    }

    @Test
    void shouldParseToMapWithMoreKeysThanValues() {
        var keys = List.of("a", "b", "c");
        var text = "1 2";

        var result = AccessLogParser.parseToMap(text, keys);

        assertThat(result).hasSize(2)
                .containsEntry("a", "1")
                .containsEntry("b", "2");
    }

    @Test
    void shouldParseToMapWithMoreValuesThanKeys() {
        var keys = List.of("a", "b");
        var text = "1 2 3 4";

        var result = AccessLogParser.parseToMap(text, keys);

        assertThat(result).hasSize(2)
                .containsEntry("a", "1")
                .containsEntry("b", "2");
    }

    @Test
    void shouldParseToMapReturnEmptyForBlankInput() {
        assertThat(AccessLogParser.parseToMap(null, List.of("a"))).isEmpty();
        assertThat(AccessLogParser.parseToMap("  ", List.of("a"))).isEmpty();
    }

    @Test
    void shouldParseToMapReturnEmptyForEmptyKeys() {
        var result = AccessLogParser.parseToMap("some text", List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldParseToBeanWithAllFields() {
        var keys = List.of("ip", "time", "request", "status", "size");
        var text = "192.168.1.1 [10/Oct/2023:13:55:36] \"GET /api\" 200 2326";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys))
                .returns("192.168.1.1", AccessLogBean::getIp)
                .returns("10/Oct/2023:13:55:36", AccessLogBean::getTime)
                .returns("GET /api", AccessLogBean::getRequest)
                .returns(200, AccessLogBean::getStatus)
                .returns(2326, AccessLogBean::getSize);
    }

    @Test
    void shouldParseToBeanWithQuotedRequestAndBracketedTime() {
        var keys = List.of("ip", "time", "request", "status");
        var text = "192.168.1.1 [10/Oct/2023:13:55:36] \"GET /\" 200";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys))
                .returns("192.168.1.1", AccessLogBean::getIp)
                .returns("10/Oct/2023:13:55:36", AccessLogBean::getTime)
                .returns("GET /", AccessLogBean::getRequest)
                .returns(200, AccessLogBean::getStatus);
    }

    @Test
    void shouldParseToBeanWithMoreKeysThanValues() {
        var keys = List.of("ip", "user", "time");
        var text = "10.0.0.1 bob";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys))
                .returns("10.0.0.1", AccessLogBean::getIp)
                .returns("bob", AccessLogBean::getUser)
                .returns(null, AccessLogBean::getTime);
    }

    @Test
    void shouldParseToBeanWithMoreValuesThanKeys() {
        var keys = List.of("ip", "status");
        var text = "10.0.0.1 200 999 extra";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys))
                .returns("10.0.0.1", AccessLogBean::getIp)
                .returns(200, AccessLogBean::getStatus);
    }

    @Test
    void shouldParseToBeanReturnNotNullForValidText() {
        var keys = List.of("ip");
        var text = "10.0.0.1";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys)).isNotNull();
    }

    @Test
    void shouldParseToBeanWithCommonLogFormat() {
        var keys = List.of("ip", "user", "time", "request", "status", "size");
        var text = "10.0.0.1 user42 [10/Oct/2023:13:55:36 +0000] \"GET /index.html HTTP/1.1\" 200 2326";

        assertThat(AccessLogParser.parseToBean(AccessLogBean.class, text, keys))
                .returns("10.0.0.1", AccessLogBean::getIp)
                .returns("user42", AccessLogBean::getUser)
                .returns("10/Oct/2023:13:55:36 +0000", AccessLogBean::getTime)
                .returns("GET /index.html HTTP/1.1", AccessLogBean::getRequest)
                .returns(200, AccessLogBean::getStatus)
                .returns(2326, AccessLogBean::getSize);
    }
}
