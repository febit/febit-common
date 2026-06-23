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

import org.febit.common.kafka.deser.StringDeserializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;
import static org.junit.jupiter.api.Assertions.*;

class KafkaOptionsTest {

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
        var options = KafkaOptions.builder()
                .topic("test-topic")
                .bootstrapServers("localhost:9092")
                .build();

        var exported = options.export();
        assertNotNull(exported);
        assertFalse(exported.isEmpty());
    }

    @Test
    void exportShouldContainBootstrapServers() {
        var options = KafkaOptions.builder()
                .topic("test-topic")
                .bootstrapServers("localhost:9092")
                .groupId("test-group")
                .build();

        var exported = options.export();
        assertEquals("localhost:9092", exported.get(BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("test-group", exported.get(GROUP_ID_CONFIG));
    }

    @Test
    void exportShouldNotContainTopicsOrPropsKeys() {
        var options = KafkaOptions.builder()
                .topic("t1").topic("t2")
                .prop("custom.prop", "custom-value")
                .bootstrapServers("localhost:9092")
                .build();

        var exported = options.export();
        assertFalse(exported.containsKey("topics"));
        assertFalse(exported.containsKey("props"));
    }

    @Test
    void exportShouldIncludeCustomProps() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .prop("custom.prop", "custom-value")
                .bootstrapServers("localhost:9092")
                .build();

        var exported = options.export();
        assertTrue(exported.containsKey("custom.prop"));
        assertEquals("custom-value", exported.get("custom.prop"));
    }

    @Test
    void exportShouldNotIncludeUnsetOptionalFields() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .build();

        var exported = options.export();
        // unset optional fields with null values are not included
        assertFalse(exported.containsKey(ENABLE_AUTO_COMMIT_CONFIG));
        assertFalse(exported.containsKey(AUTO_OFFSET_RESET_CONFIG));
        assertFalse(exported.containsKey(SECURITY_PROTOCOL_CONFIG));
        assertFalse(exported.containsKey(KEY_DESERIALIZER_CLASS_CONFIG));
        assertFalse(exported.containsKey(VALUE_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void exportShouldIncludeMaxPollRecordsAsZero() {
        // Note: primitive int defaults to 0 in Jackson toNamedMap
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .build();

        var exported = options.export();
        assertEquals(0, exported.get(MAX_POLL_RECORDS_CONFIG));
    }

    @Test
    void exportShouldIncludeExplicitlySetValues() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .enableAutoCommit(true)
                .autoOffsetReset("earliest")
                .maxPollRecords(200)
                .build();

        var exported = options.export();
        assertEquals(true, exported.get(ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("earliest", exported.get(AUTO_OFFSET_RESET_CONFIG));
        assertEquals(200, exported.get(MAX_POLL_RECORDS_CONFIG));
    }

    @Test
    void exportShouldIncludeSecurityProtocolWhenSet() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .securityProtocol("SASL_SSL")
                .build();

        var exported = options.export();
        assertEquals("SASL_SSL", exported.get(SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void exportShouldIncludeSslSettingsWhenSet() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .sslTruststoreLocation("/path/to/truststore")
                .sslTruststorePassword("secret")
                .sslEndpointIdentificationAlgorithm("HTTPS")
                .build();

        var exported = options.export();
        assertEquals("/path/to/truststore", exported.get(SSL_TRUSTSTORE_LOCATION_CONFIG));
        assertEquals("secret", exported.get(SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertEquals("HTTPS", exported.get(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    }

    @Test
    void exportShouldIncludeSaslSettingsWhenSet() {
        var options = KafkaOptions.builder()
                .topic("t1")
                .bootstrapServers("localhost:9092")
                .saslMechanism("PLAIN")
                .saslJaasConfig("org.apache.kafka.common.security.plain.PlainLoginModule required;")
                .build();

        var exported = options.export();
        assertEquals("PLAIN", exported.get(SASL_MECHANISM));
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required;",
                exported.get(SASL_JAAS_CONFIG));
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
