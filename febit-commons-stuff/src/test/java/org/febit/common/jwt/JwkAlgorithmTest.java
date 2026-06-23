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

import com.nimbusds.jose.JWSAlgorithm;
import org.febit.lang.security.SecurityAlgorithm;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;

import static org.assertj.core.api.Assertions.assertThat;

class JwkAlgorithmTest {

    @Test
    void allAlgorithmsShouldHaveJwsAlgorithm() {
        for (var alg : JwkAlgorithm.values()) {
            assertThat(alg.getJws()).isNotNull();
        }
    }

    @Test
    void allAlgorithmsShouldHaveSecurityAlgorithm() {
        for (var alg : JwkAlgorithm.values()) {
            assertThat(alg.getSecurity()).isNotNull();
        }
    }

    @Test
    void allAlgorithmsShouldHaveSignerFactory() {
        for (var alg : JwkAlgorithm.values()) {
            assertThat(alg.getSignerFactory()).isNotNull();
        }
    }

    @Test
    void rsaAlgorithmsShouldUseSecurityAlgorithmRsa() {
        assertThat(JwkAlgorithm.RS256.getSecurity()).isEqualTo(SecurityAlgorithm.RSA);
        assertThat(JwkAlgorithm.RS384.getSecurity()).isEqualTo(SecurityAlgorithm.RSA);
        assertThat(JwkAlgorithm.RS512.getSecurity()).isEqualTo(SecurityAlgorithm.RSA);
    }

    @Test
    void ecAlgorithmsShouldUseSecurityAlgorithmEc() {
        assertThat(JwkAlgorithm.ES256.getSecurity()).isEqualTo(SecurityAlgorithm.EC);
        assertThat(JwkAlgorithm.ES256K.getSecurity()).isEqualTo(SecurityAlgorithm.EC);
        assertThat(JwkAlgorithm.ES384.getSecurity()).isEqualTo(SecurityAlgorithm.EC);
        assertThat(JwkAlgorithm.ES512.getSecurity()).isEqualTo(SecurityAlgorithm.EC);
    }

    @Test
    void jwsAlgorithmsShouldMatchExpected() {
        assertThat(JwkAlgorithm.RS256.getJws()).isEqualTo(JWSAlgorithm.RS256);
        assertThat(JwkAlgorithm.RS384.getJws()).isEqualTo(JWSAlgorithm.RS384);
        assertThat(JwkAlgorithm.RS512.getJws()).isEqualTo(JWSAlgorithm.RS512);
        assertThat(JwkAlgorithm.ES256.getJws()).isEqualTo(JWSAlgorithm.ES256);
        assertThat(JwkAlgorithm.ES256K.getJws()).isEqualTo(JWSAlgorithm.ES256K);
        assertThat(JwkAlgorithm.ES384.getJws()).isEqualTo(JWSAlgorithm.ES384);
        assertThat(JwkAlgorithm.ES512.getJws()).isEqualTo(JWSAlgorithm.ES512);
    }

    @Test
    void shouldHaveSevenAlgorithms() {
        assertThat(JwkAlgorithm.values()).hasSize(7);
    }

    @Test
    void rsaSignerFactoryShouldCreateSigner() throws Exception {
        var keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        var keyPair = keyPairGen.generateKeyPair();
        var signer = JwkAlgorithm.RS256.getSignerFactory().create((RSAPrivateKey) keyPair.getPrivate());

        assertThat(signer).isNotNull();
    }
}
