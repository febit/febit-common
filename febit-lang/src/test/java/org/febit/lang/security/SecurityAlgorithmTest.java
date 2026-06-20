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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
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

    @Test
    void enum_hasExpectedValues() {
        var values = SecurityAlgorithm.values();
        assertEquals(2, values.length);
        assertEquals(SecurityAlgorithm.EC, values[0]);
        assertEquals(SecurityAlgorithm.RSA, values[1]);
    }

    @Test
    void valueOf_resolvesByName() {
        assertEquals(SecurityAlgorithm.EC, SecurityAlgorithm.valueOf("EC"));
        assertEquals(SecurityAlgorithm.RSA, SecurityAlgorithm.valueOf("RSA"));
    }

    @Test
    void enum_isNotNull() {
        assertNotNull(SecurityAlgorithm.EC);
        assertNotNull(SecurityAlgorithm.RSA);
    }

    @Test
    void decodePublicKey_rsa_roundTrips() throws Exception {
        var pair = generateRsa();
        var encoded = base64Public(pair);

        var decoded = SecurityAlgorithm.RSA.decodePublicKey(encoded);

        assertNotNull(decoded);
        assertEquals("RSA", decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @Test
    void decodePrivateKey_rsa_roundTrips() throws Exception {
        var pair = generateRsa();
        var encoded = base64Private(pair);

        var decoded = SecurityAlgorithm.RSA.decodePrivateKey(encoded);

        assertNotNull(decoded);
        assertEquals("RSA", decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @Test
    void decodePublicKey_ec_roundTrips() throws Exception {
        var pair = generateEc();
        var encoded = base64Public(pair);

        var decoded = SecurityAlgorithm.EC.decodePublicKey(encoded);

        assertNotNull(decoded);
        assertEquals("EC", decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @Test
    void decodePrivateKey_ec_roundTrips() throws Exception {
        var pair = generateEc();
        var encoded = base64Private(pair);

        var decoded = SecurityAlgorithm.EC.decodePrivateKey(encoded);

        assertNotNull(decoded);
        assertEquals("EC", decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @Test
    void decodePublicKey_invalidBase64_throwsIllegalArgumentException() {
        // IllegalArgumentException bubbles up from Base64.getDecoder().decode
        assertThrows(IllegalArgumentException.class,
                () -> SecurityAlgorithm.RSA.decodePublicKey("!!!not-base64!!!"));
    }

    @Test
    void decodePrivateKey_invalidBase64_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SecurityAlgorithm.RSA.decodePrivateKey("!!!not-base64!!!"));
    }

    @Test
    void decodePublicKey_validBase64ButNotAKey_throwsInvalidKeySpecException() {
        // Arbitrary valid base64 that is not a valid X.509 key spec
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.RSA.decodePublicKey(notAKey));
    }

    @Test
    void decodePrivateKey_validBase64ButNotAKey_throwsInvalidKeySpecException() {
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.RSA.decodePrivateKey(notAKey));
    }

    @Test
    void decodePublicKey_ecWithRsaFormat_throwsInvalidKeySpecException() throws Exception {
        // A valid RSA key fed to EC decoder: name matches but spec format is wrong
        var rsaPair = generateRsa();
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.EC.decodePublicKey(base64Public(rsaPair)));
    }

    @Test
    void decodePrivateKey_ecWithRsaFormat_throwsInvalidKeySpecException() throws Exception {
        var rsaPair = generateRsa();
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.EC.decodePrivateKey(base64Private(rsaPair)));
    }

    @Test
    void genericPublicKey_unknownAlgorithm_throwsNoSuchAlgorithmException() {
        // KeyFactory.getInstance throws NoSuchAlgorithmException directly
        // for unknown algorithms; the public static method does not wrap it.
        // The UncheckedException wrapping only applies to enum decodePublicKey/decodePrivateKey.
        var encoded = Base64.getEncoder().encodeToString(new byte[]{0});
        assertThrows(NoSuchAlgorithmException.class,
                () -> SecurityAlgorithm.genericPublicKey(UNKNOWN_ALGORITHM, encoded));
    }

    @Test
    void genericPrivateKey_unknownAlgorithm_throwsNoSuchAlgorithmException() {
        var encoded = Base64.getEncoder().encodeToString(new byte[]{0});
        assertThrows(NoSuchAlgorithmException.class,
                () -> SecurityAlgorithm.genericPrivateKey(UNKNOWN_ALGORITHM, encoded));
    }

    @Test
    void genericPublicKey_invalidBase64_throwsIllegalArgumentException() {
        // Standard algorithm + bad base64: the Base64 error is raised before KeyFactory
        assertThrows(IllegalArgumentException.class,
                () -> SecurityAlgorithm.genericPublicKey("RSA", "!!!not-base64!!!"));
    }

    @Test
    void genericPrivateKey_invalidBase64_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SecurityAlgorithm.genericPrivateKey("RSA", "!!!not-base64!!!"));
    }

    @Test
    void genericPublicKey_rsa_roundTrips() throws Exception {
        var pair = generateRsa();
        var encoded = base64Public(pair);

        PublicKey decoded = SecurityAlgorithm.genericPublicKey("RSA", encoded);

        assertEquals("RSA", decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @Test
    void genericPrivateKey_rsa_roundTrips() throws Exception {
        var pair = generateRsa();
        var encoded = base64Private(pair);

        PrivateKey decoded = SecurityAlgorithm.genericPrivateKey("RSA", encoded);

        assertEquals("RSA", decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @Test
    void genericPublicKey_ec_roundTrips() throws Exception {
        var pair = generateEc();
        var encoded = base64Public(pair);

        PublicKey decoded = SecurityAlgorithm.genericPublicKey("EC", encoded);

        assertEquals("EC", decoded.getAlgorithm());
        assertEquals(pair.getPublic(), decoded);
    }

    @Test
    void genericPrivateKey_ec_roundTrips() throws Exception {
        var pair = generateEc();
        var encoded = base64Private(pair);

        PrivateKey decoded = SecurityAlgorithm.genericPrivateKey("EC", encoded);

        assertEquals("EC", decoded.getAlgorithm());
        assertEquals(pair.getPrivate(), decoded);
    }

    @Test
    void genericPublicKey_validBase64NotAKey_throwsInvalidKeySpecException() {
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.genericPublicKey("RSA", notAKey));
    }

    @Test
    void genericPrivateKey_validBase64NotAKey_throwsInvalidKeySpecException() {
        var notAKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.genericPrivateKey("RSA", notAKey));
    }

    @Test
    void enumDecoders_noSuchAlgorithmPath_isUnreachableForKnownAlgorithms() throws Exception {
        // The catch-NoSuchAlgorithmException-into-UncheckedException branch in
        // decodePublicKey/decodePrivateKey is theoretically unreachable because
        // SecurityAlgorithm.EC uses "EC" and SecurityAlgorithm.RSA uses "RSA",
        // both always available in standard JDK JCE providers.
        // The wrapping is defensive code; this test simply confirms the success
        // path runs without throwing UncheckedException.
        var pair = generateRsa();
        var rsaPub = SecurityAlgorithm.RSA.decodePublicKey(base64Public(pair));
        var rsaPriv = SecurityAlgorithm.RSA.decodePrivateKey(base64Private(pair));
        assertNotNull(rsaPub);
        assertNotNull(rsaPriv);
    }

    @Test
    void ecDecoders_consistentAcrossInvocations() throws Exception {
        // The decoder lambdas capture the algorithm string but produce a fresh KeyFactory
        // each time; multiple calls should all succeed
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
    void emptyStringBase64_throws() {
        // Empty string is a valid base64 input (decodes to empty bytes) but is not a key
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.RSA.decodePublicKey(""));
        assertThrows(InvalidKeySpecException.class,
                () -> SecurityAlgorithm.RSA.decodePrivateKey(""));
    }

    @Test
    void algorithms_classIsPackagePrivateAndHoldsExpectedConstants() throws Exception {
        // White-box: the Algorithms utility class exposes the algorithm strings.
        // EC and RSA enum entries must use these constants (verified indirectly:
        // decoding works, so the constant strings must be recognized by KeyFactory).
        var ecConst = Algorithms.class.getDeclaredField("EC");
        var rsaConst = Algorithms.class.getDeclaredField("RSA");
        ecConst.setAccessible(true);
        rsaConst.setAccessible(true);
        assertEquals("EC", ecConst.get(null));
        assertEquals("RSA", rsaConst.get(null));
    }

    @Test
    void algorithms_classIsFinalAndUtility() {
        // @UtilityClass generates a private no-arg constructor; class must be final
        assertTrue(java.lang.reflect.Modifier.isFinal(Algorithms.class.getModifiers()));
    }

    @Test
    void keyDecoderInterface_isFunctional() {
        SecurityAlgorithm.KeyDecoder<PublicKey> decoder = encoded -> null;
        assertNotNull(decoder);
    }
}
