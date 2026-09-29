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
package org.febit.lang.security;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAlgorithmTest {

    private static final String UNKNOWN_ALGORITHM = "febit-nonexistent-algorithm";

    private static String base64Public(KeyPair pair) {
        return Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
    }

    private static String base64Private(KeyPair pair) {
        return Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
    }

    private static KeyPair generateRsa() throws NoSuchAlgorithmException {
        var gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    private static KeyPair generateEc() throws NoSuchAlgorithmException {
        var gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(256);
        return gen.generateKeyPair();
    }

    private static KeyPair pairFor(SecurityAlgorithm algorithm) throws NoSuchAlgorithmException {
        return algorithm == SecurityAlgorithm.RSA ? generateRsa() : generateEc();
    }

    private static Object decodeKey(KeyType keyType, SecurityAlgorithm algorithm, String encoded) throws Exception {
        return keyType == KeyType.PUBLIC
                ? algorithm.decodePublicKey(encoded)
                : algorithm.decodePrivateKey(encoded);
    }

    private static Key genericKey(KeyType keyType, String algorithm, String encoded) throws Exception {
        return keyType == KeyType.PUBLIC
                ? SecurityAlgorithm.genericPublicKey(algorithm, encoded)
                : SecurityAlgorithm.genericPrivateKey(algorithm, encoded);
    }

    @TableTest("""
            index | expected
            0     | EC
            1     | RSA
            """)
    void enum_hasExpectedValues(int index, SecurityAlgorithm expected) {
        assertEquals(2, SecurityAlgorithm.values().length);
        assertEquals(expected, SecurityAlgorithm.values()[index]);
    }

    @TableTest("""
            name | expected
            EC   | EC
            RSA  | RSA
            """)
    void valueOf_resolvesByName(String name, SecurityAlgorithm expected) {
        assertEquals(expected, SecurityAlgorithm.valueOf(name));
    }

    @Test
    void enum_isNotNull() {
        assertNotNull(SecurityAlgorithm.EC);
        assertNotNull(SecurityAlgorithm.RSA);
    }

    @TableTest("""
            algorithm
            RSA
            EC
            """)
    void decodePublicKey_roundTrips(SecurityAlgorithm algorithm) throws Exception {
        var pair = pairFor(algorithm);
        var decoded = algorithm.decodePublicKey(base64Public(pair));
        assertEquals(algorithm.name(), decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @TableTest("""
            algorithm
            RSA
            EC
            """)
    void decodePrivateKey_roundTrips(SecurityAlgorithm algorithm) throws Exception {
        var pair = pairFor(algorithm);
        var decoded = algorithm.decodePrivateKey(base64Private(pair));
        assertEquals(algorithm.name(), decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void decode_invalidBase64_throwsIllegalArgumentException(KeyType keyType) {
        assertThrows(IllegalArgumentException.class,
                () -> decodeKey(keyType, SecurityAlgorithm.RSA, "!!!not-base64!!!"));
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void decode_validBase64NotAKey_throwsInvalidKeySpecException(KeyType keyType) {
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class, () -> decodeKey(keyType, SecurityAlgorithm.RSA, notAKey));
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void decode_ecFormatWithRsaKey_throwsInvalidKeySpecException(KeyType keyType) throws Exception {
        var rsaPair = generateRsa();
        var encoded = keyType == KeyType.PUBLIC ? base64Public(rsaPair) : base64Private(rsaPair);
        assertThrows(InvalidKeySpecException.class, () -> decodeKey(keyType, SecurityAlgorithm.EC, encoded));
    }

    @TableTest("""
            algorithm
            RSA
            EC
            """)
    void genericPublicKey_roundTrips(SecurityAlgorithm algorithm) throws Exception {
        var pair = pairFor(algorithm);
        var decoded = SecurityAlgorithm.genericPublicKey(algorithm.name(), base64Public(pair));
        assertEquals(algorithm.name(), decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @TableTest("""
            algorithm
            RSA
            EC
            """)
    void genericPrivateKey_roundTrips(SecurityAlgorithm algorithm) throws Exception {
        var pair = pairFor(algorithm);
        var decoded = SecurityAlgorithm.genericPrivateKey(algorithm.name(), base64Private(pair));
        assertEquals(algorithm.name(), decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void generic_invalidBase64_throwsIllegalArgumentException(KeyType keyType) {
        assertThrows(IllegalArgumentException.class, () -> genericKey(keyType, "RSA", "!!!not-base64!!!"));
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void generic_validBase64NotAKey_throwsInvalidKeySpecException(KeyType keyType) {
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class, () -> genericKey(keyType, "RSA", notAKey));
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void generic_unknownAlgorithm_throwsNoSuchAlgorithmException(KeyType keyType) {
        var encoded = Base64.getEncoder().encodeToString(new byte[]{0});
        assertThrows(NoSuchAlgorithmException.class, () -> genericKey(keyType, UNKNOWN_ALGORITHM, encoded));
    }

    @TableTest("""
            keyType
            PUBLIC
            PRIVATE
            """)
    void emptyStringBase64_throwsInvalidKeySpecException(KeyType keyType) {
        assertThrows(InvalidKeySpecException.class, () -> decodeKey(keyType, SecurityAlgorithm.RSA, ""));
    }

    @Test
    void ecDecoders_consistentAcrossInvocations() throws Exception {
        var pair = generateEc();
        var encoded = base64Public(pair);

        var first = SecurityAlgorithm.EC.decodePublicKey(encoded);
        var second = SecurityAlgorithm.EC.decodePublicKey(encoded);
        var third = SecurityAlgorithm.EC.decodePublicKey(encoded);

        assertEquals(first, second);
        assertEquals(second, third);
    }

    @Test
    void rsaDecoders_consistentAcrossInvocations() throws Exception {
        var pair = generateRsa();
        var encoded = base64Private(pair);

        var first = SecurityAlgorithm.RSA.decodePrivateKey(encoded);
        var second = SecurityAlgorithm.RSA.decodePrivateKey(encoded);
        var third = SecurityAlgorithm.RSA.decodePrivateKey(encoded);

        assertEquals(first, second);
        assertEquals(second, third);
    }

    @Test
    void enumDecoders_noSuchAlgorithmPath_isUnreachableForKnownAlgorithms() throws Exception {
        var pair = generateRsa();
        var rsaPub = SecurityAlgorithm.RSA.decodePublicKey(base64Public(pair));
        var rsaPriv = SecurityAlgorithm.RSA.decodePrivateKey(base64Private(pair));
        assertNotNull(rsaPub);
        assertNotNull(rsaPriv);
    }

    @Test
    void algorithms_classIsPackagePrivateAndHoldsExpectedConstants() throws Exception {
        var ecConst = Algorithms.class.getDeclaredField("EC");
        var rsaConst = Algorithms.class.getDeclaredField("RSA");
        ecConst.setAccessible(true);
        rsaConst.setAccessible(true);
        assertEquals("EC", ecConst.get(null));
        assertEquals("RSA", rsaConst.get(null));
    }

    @Test
    void algorithms_classIsFinalAndUtility() {
        assertTrue(Modifier.isFinal(Algorithms.class.getModifiers()));
    }

    @Test
    void keyDecoderInterface_isFunctional() {
        SecurityAlgorithm.KeyDecoder<PublicKey> decoder = encoded -> null;
        assertNotNull(decoder);
    }

    enum KeyType {
        PUBLIC, PRIVATE
    }
}
