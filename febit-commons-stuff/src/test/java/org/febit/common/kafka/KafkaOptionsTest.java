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
package org.febit.common.kafka;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import org.febit.common.kafka.deser.StringDeserializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KafkaOptionsTest {

    private static KafkaOptions fullOptions() {
        return KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .groupId("g1")
                .maxPollRecords(200)
                .autoOffsetReset("earliest")
                .enableAutoCommit(true)
                .securityProtocol("SSL")
                .sslTruststoreLocation("/ts")
                .sslTruststorePassword("pw")
                .sslEndpointIdentificationAlgorithm("HTTPS")
                .saslMechanism("PLAIN")
                .saslJaasConfig("module")
                .prop("custom.prop", "custom-value")
                .build();
    }

    private static KafkaOptions minimalOptions() {
        return KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .build();
    }

    @Test
    void shouldBuildWithRequiredFields() {
        var options = KafkaOptions.builder()
                .topic("test-topic")
                .bootstrapServers("localhost:9092")
                .build();

        assertEquals(List.of("test-topic"), options.getTopics());
        assertEquals("localhost:9092", options.getBootstrapServers());
    }

    @Test
    void shouldHaveDefaultValues() {
        var options = new KafkaOptions();
        options.setTopics(List.of("test-topic"));
        options.setBootstrapServers("localhost:9092");

        assertEquals(500, options.getMaxPollRecords());
        assertEquals("latest", options.getAutoOffsetReset());
        assertEquals(StringDeserializer.class, options.getKeyDeserializer());
        assertEquals(StringDeserializer.class, options.getValueDeserializer());
    }

    @Test
    void shouldBuildMultipleTopics() {
        var options = KafkaOptions.builder()
                .topic("t1").topic("t2").topic("t3")
                .bootstrapServers("localhost:9092")
                .build();

        assertEquals(List.of("t1", "t2", "t3"), options.getTopics());
    }

    @Test
    void shouldBuildWithGroupId() {
        var options = KafkaOptions.builder()
                .topic("test-topic")
                .bootstrapServers("localhost:9092")
                .groupId("my-group")
                .build();

        assertEquals("my-group", options.getGroupId());
    }

    @Test
    void shouldBuildWithCustomProps() {
        var options = KafkaOptions.builder()
                .topic("test-topic")
                .bootstrapServers("localhost:9092")
                .prop("custom.key", "custom-value")
                .prop("another.key", "another-value")
                .build();

        assertTrue(options.getProps().containsKey("custom.key"));
        assertEquals("custom-value", options.getProps().get("custom.key"));
        assertEquals("another-value", options.getProps().get("another.key"));
    }

    @Test
    void exportShouldNotBeNull() {
        var exported = minimalOptions().export();
        assertNotNull(exported);
        assertFalse(exported.isEmpty());
    }

    @Test
    void exportShouldNotContainTopicsOrPropsKeys() {
        var exported = KafkaOptions.builder()
                .topic("t1").topic("t2")
                .prop("custom.prop", "custom-value")
                .bootstrapServers("localhost:9092")
                .build()
                .export();

        assertFalse(exported.containsKey("topics"));
        assertFalse(exported.containsKey("props"));
    }

    @TableTest("""
            configKey                             | expected
            bootstrap.servers                     | localhost:9092
            group.id                              | g1
            auto.offset.reset                     | earliest
            security.protocol                     | SSL
            ssl.truststore.location               | /ts
            ssl.truststore.password               | pw
            ssl.endpoint.identification.algorithm | HTTPS
            sasl.mechanism                        | PLAIN
            sasl.jaas.config                      | module
            custom.prop                           | custom-value
            """)
    void exportStringConfigs(String configKey, String expected) {
        assertEquals(expected, fullOptions().export().get(configKey));
    }

    @TableTest("""
            configKey        | setup   | expected
            max.poll.records | full    | 200
            max.poll.records | minimal | 0
            """)
    void exportMaxPollRecords(String configKey, String setup, int expected) {
        var options = "full".equals(setup) ? fullOptions() : minimalOptions();
        assertEquals(expected, options.export().get(configKey));
    }

    @TableTest("""
            configKey          | expected
            enable.auto.commit | true
            """)
    void exportEnableAutoCommit(String configKey, boolean expected) {
        assertEquals(expected, fullOptions().export().get(configKey));
    }

    @TableTest("""
            configKey
            enable.auto.commit
            auto.offset.reset
            security.protocol
            key.deserializer
            value.deserializer
            """)
    void exportExcludesUnsetOptionalConfigs(String configKey) {
        assertFalse(minimalOptions().export().containsKey(configKey));
    }

    @Test
    void shouldBuildWithAllFields() {
        var options = KafkaOptions.builder()
                .topic("t1").topic("t2")
                .bootstrapServers("kafka:9092")
                .groupId("g1")
                .maxPollRecords(100)
                .autoOffsetReset("earliest")
                .securityProtocol("SSL")
                .sslTruststoreLocation("/ts")
                .sslTruststorePassword("pw")
                .sslEndpointIdentificationAlgorithm("")
                .saslMechanism("PLAIN")
                .saslJaasConfig("module")
                .prop("p1", "v1")
                .prop("p2", "v2")
                .build();

        assertEquals(List.of("t1", "t2"), options.getTopics());
        assertEquals("kafka:9092", options.getBootstrapServers());
        assertEquals("g1", options.getGroupId());
        assertEquals(100, options.getMaxPollRecords());
        assertEquals("earliest", options.getAutoOffsetReset());
        assertEquals("SSL", options.getSecurityProtocol());
        assertEquals("/ts", options.getSslTruststoreLocation());
        assertEquals("pw", options.getSslTruststorePassword());
        assertEquals("", options.getSslEndpointIdentificationAlgorithm());
        assertEquals("PLAIN", options.getSaslMechanism());
        assertEquals("module", options.getSaslJaasConfig());
        assertEquals(2, options.getProps().size());
    }

    @Test
    void builderShouldHaveStaticFactoryMethod() {
        var builder = KafkaOptions.builder();
        assertNotNull(builder);
        var options = builder
                .topic("t1")
                .bootstrapServers("kafka:9092")
                .build();
        assertNotNull(options);
    }
}
