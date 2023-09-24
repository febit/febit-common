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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.apache.kafka.common.serialization.Deserializer;
import org.febit.common.jcommander.IOptions;
import org.febit.common.kafka.deser.StringDeserializer;
import org.febit.lang.util.JacksonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_DOC;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_DOC;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_DOC;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG_DOC;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM_DOC;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_DOC;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_DOC;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_DOC;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class KafkaOptions implements IOptions {

    @Parameter(
            names = {"--kafka-topics"},
            required = true,
            description = "Kafka topics, split by COMMA, like: topic1,topic2"
    )
    @Singular
    private List<String> topics = new ArrayList<>();

    @DynamicParameter(
            names = "--kafka:",
            description = "kafka addon conf"
    )
    @Singular
    private Map<String, String> props = new HashMap<>();

    @Parameter(
            names = {"--kafka-bootstrap-servers"},
            required = true,
            description = BOOTSTRAP_SERVERS_DOC
    )
    @JsonProperty(BOOTSTRAP_SERVERS_CONFIG)
    private String bootstrapServers;

    @Parameter(
            names = {"--kafka-group-id"},
            description = GROUP_ID_DOC
    )
    @JsonProperty(GROUP_ID_CONFIG)
    private String groupId;

    @Parameter(
            names = {"--kafka-max-poll-records"},
            description = "The maximum number of records returned in a single call to poll()."
    )
    @JsonProperty(MAX_POLL_RECORDS_CONFIG)
    private int maxPollRecords = 500;

    @Parameter(
            names = {"--kafka-auto-offset-reset"},
            description = AUTO_OFFSET_RESET_DOC
    )
    @JsonProperty(AUTO_OFFSET_RESET_CONFIG)
    private String autoOffsetReset = "latest";

    @Parameter(
            names = {"--kafka-security-protocol"},
            description = "Protocol used to communicate with brokers."
                    + " Valid values are: PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL."
    )
    @JsonProperty(SECURITY_PROTOCOL_CONFIG)
    private String securityProtocol;

    @Parameter(
            names = {"--kafka-ssl-truststore-location"},
            description = SSL_TRUSTSTORE_LOCATION_DOC
    )
    @JsonProperty(SSL_TRUSTSTORE_LOCATION_CONFIG)
    private String sslTruststoreLocation;

    @Parameter(
            names = {"--kafka-ssl-truststore-password"},
            description = SSL_TRUSTSTORE_PASSWORD_DOC
    )
    @JsonProperty(SSL_TRUSTSTORE_PASSWORD_CONFIG)
    private String sslTruststorePassword;

    @Parameter(
            names = {"--kafka-ssl-endpoint-identification-algorithm"},
            description = SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_DOC
    )
    @JsonProperty(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG)
    private String sslEndpointIdentificationAlgorithm;

    @Parameter(
            names = {"--kafka-sasl-mechanism"},
            description = SASL_MECHANISM_DOC
    )
    @JsonProperty(SASL_MECHANISM)
    private String saslMechanism;

    @Parameter(
            names = {"--kafka-sasl-jaas-config"},
            description = SASL_JAAS_CONFIG_DOC
    )
    @JsonProperty(SASL_JAAS_CONFIG)
    private String saslJaasConfig;

    @JsonProperty(KEY_DESERIALIZER_CLASS_CONFIG)
    private Class<? extends Deserializer<?>> keyDeserializer = StringDeserializer.class;

    @JsonProperty(VALUE_DESERIALIZER_CLASS_CONFIG)
    private Class<? extends Deserializer<?>> valueDeserializer = StringDeserializer.class;

    @JsonProperty(ENABLE_AUTO_COMMIT_CONFIG)
    private Boolean enableAutoCommit = false;

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> export() {
        var result = new HashMap<String, Object>();
        if (props != null) {
            result.putAll(props);
        }

        var buildIn = JacksonUtils.toNamedMap(this);
        Objects.requireNonNull(buildIn);
        buildIn.remove("topics");
        buildIn.remove("props");
        result.putAll(buildIn);
        return result;
    }
}
