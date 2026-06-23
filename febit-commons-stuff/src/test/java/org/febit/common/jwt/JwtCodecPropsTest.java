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
package org.febit.common.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCodecPropsTest {

    @Test
    void shouldBuildWithNoKeys() {
        var props = JwtCodecProps.builder().build();

        assertThat(props.keys()).isEmpty();
        assertThat(props.signerKeyId()).isNull();
    }

    @Test
    void shouldBuildWithSignerKeyId() {
        var props = JwtCodecProps.builder()
                .signerKeyId("key-1")
                .build();

        assertThat(props.signerKeyId()).isEqualTo("key-1");
    }

    @Test
    void shouldBuildWithSingleKey() {
        var key = JwtKey.builder()
                .id("k1")
                .algorithm(JwkAlgorithm.RS256)
                .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
                .build();

        var props = JwtCodecProps.builder()
                .key(key)
                .build();

        assertThat(props.keys()).containsExactly(key);
    }

    @Test
    void shouldBuildWithMultipleKeys() {
        var key1 = JwtKey.builder()
                .id("k1")
                .algorithm(JwkAlgorithm.RS256)
                .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
                .build();
        var key2 = JwtKey.builder()
                .id("k2")
                .algorithm(JwkAlgorithm.ES256)
                .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
                .build();

        var props = JwtCodecProps.builder()
                .key(key1)
                .key(key2)
                .build();

        assertThat(props.keys()).containsExactly(key1, key2);
    }

    @Test
    void toBuilderShouldPreserveValues() {
        var key = JwtKey.builder()
                .id("k1")
                .algorithm(JwkAlgorithm.RS256)
                .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
                .build();
        var original = JwtCodecProps.builder()
                .signerKeyId("signer-1")
                .key(key)
                .build();

        var modified = original.toBuilder()
                .signerKeyId("signer-2")
                .build();

        assertThat(modified.signerKeyId()).isEqualTo("signer-2");
        assertThat(modified.keys()).isEqualTo(original.keys());
    }
}
