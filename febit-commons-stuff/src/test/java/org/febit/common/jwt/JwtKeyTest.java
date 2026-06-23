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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtKeyTest {

    static final JwtKey ES_VALID = JwtKey.builder()
            .id("es1")
            .algorithm(JwkAlgorithm.ES256)
            .signerKey("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBSJwcMG9Ih5VeuXTv0yMPLf1B70/kWFe+ZzbZijwoWDA==")
            .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
            .build();

    static final JwtKey ES_NO_SIGNER = JwtKey.builder()
            .id("es-no-signer")
            .algorithm(JwkAlgorithm.ES256)
            .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
            .build();

    @Test
    void shouldResolveWithSignerAndVerifier() {
        var resolved = ES_VALID.resolve();

        assertEquals("es1", resolved.id());
        assertEquals(JwkAlgorithm.ES256, resolved.algorithm());

        assertTrue(resolved.signerKey().isPresent());
        assertTrue(resolved.verifierKey().isPresent());
        assertTrue(resolved.signer().isPresent());
    }

    @Test
    void shouldResolveWithoutSignerKey() {
        var resolved = ES_NO_SIGNER.resolve();

        assertFalse(resolved.signerKey().isPresent());
        assertTrue(resolved.verifierKey().isPresent());
        assertFalse(resolved.signer().isPresent());
    }

    @Test
    void shouldResolveJwsAlgorithm() {
        var resolved = ES_VALID.resolve();

        assertEquals(JwkAlgorithm.ES256.getJws(), resolved.jwsAlgorithm());
    }

    @Test
    void shouldThrowForInvalidVerifierKey() {
        var key = JwtKey.builder()
                .id("bad")
                .algorithm(JwkAlgorithm.ES256)
                .verifierKey("INVALID_KEY")
                .build();

        assertThrows(RuntimeException.class, key::resolve);
    }

    @Test
    void shouldThrowForInvalidSignerKey() {
        var key = JwtKey.builder()
                .id("bad")
                .algorithm(JwkAlgorithm.ES256)
                .signerKey("INVALID_KEY")
                .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
                .build();

        assertThrows(RuntimeException.class, key::resolve);
    }

    @Test
    void shouldUseToBuilder() {
        var modified = ES_VALID.toBuilder()
                .id("modified")
                .build();

        assertEquals("modified", modified.id());
        assertEquals(JwkAlgorithm.ES256, modified.algorithm());
        assertEquals(ES_VALID.signerKey(), modified.signerKey());
        assertEquals(ES_VALID.verifierKey(), modified.verifierKey());
    }

    @Test
    void resolvedVerifierShouldThrowWhenNoVerifierKey() {
        var resolved = JwtKey.builder()
                .id("no-verifier")
                .algorithm(JwkAlgorithm.ES256)
                .build()
                .resolve();

        var header = new JWSHeader.Builder(JwkAlgorithm.ES256.getJws()).keyID("no-verifier").build();
        assertThrows(JOSEException.class, () -> resolved.verifier(header));
    }
}
