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
import org.tabletest.junit.TableTest;

import org.febit.lang.security.SecurityAlgorithm;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;

import static org.junit.jupiter.api.Assertions.*;

class JwkAlgorithmTest {

    @TableTest("""
            alg
            RS256
            RS384
            RS512
            ES256
            ES256K
            ES384
            ES512
            """)
    void propertiesAreNotNull(JwkAlgorithm alg) {
        assertNotNull(alg.getJws());
        assertNotNull(alg.getSecurity());
        assertNotNull(alg.getSignerFactory());
    }

    @TableTest("""
            alg    | expected
            RS256  | RSA
            RS384  | RSA
            RS512  | RSA
            ES256  | EC
            ES256K | EC
            ES384  | EC
            ES512  | EC
            """)
    void securityAlgorithm(JwkAlgorithm alg, SecurityAlgorithm expected) {
        assertEquals(expected, alg.getSecurity());
    }

    @TableTest("""
            alg    | expected
            RS256  | RS256
            RS384  | RS384
            RS512  | RS512
            ES256  | ES256
            ES256K | ES256K
            ES384  | ES384
            ES512  | ES512
            """)
    void jwsAlgorithmName(JwkAlgorithm alg, String expected) {
        assertEquals(expected, alg.getJws().getName());
    }

    @Test
    void shouldHaveSevenAlgorithms() {
        assertEquals(7, JwkAlgorithm.values().length);
    }

    @Test
    void rsaSignerFactoryShouldCreateSigner() throws Exception {
        var keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        var keyPair = keyPairGen.generateKeyPair();
        var signer = JwkAlgorithm.RS256.getSignerFactory().create((RSAPrivateKey) keyPair.getPrivate());

        assertNotNull(signer);
    }
}
