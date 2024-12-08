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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.febit.lang.UncheckedException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@lombok.Builder(
        builderClassName = "Builder",
        toBuilder = true
)
public record JwtKey(
        @Nonnull String id,
        @Nonnull JwkAlgorithm algorithm,
        @Nullable String verifierKey,
        @Nullable String signerKey
) {

    public record Resolved(
            @Nonnull String id,
            @Nonnull JwkAlgorithm algorithm,
            @Nonnull Optional<PrivateKey> signerKey,
            @Nonnull Optional<PublicKey> verifierKey,
            @Nonnull Optional<JWSSigner> signer
    ) {
        public JWSAlgorithm jwsAlgorithm() {
            return this.algorithm.getJws();
        }

        public JWSVerifier verifier(JWSHeader header) throws JOSEException {
            var key = verifierKey.orElseThrow(() ->
                    new JOSEException("No verifier key found for key id: " + id)
            );
            return new DefaultJWSVerifierFactory()
                    .createJWSVerifier(header, key);
        }
    }

    public Resolved resolve() {
        var signerKey = Optional.ofNullable(signerKey())
                .map(this::buildSignerKey);
        var verifierKey = Optional.ofNullable(verifierKey())
                .map(this::buildVerifierKey);
        var signer = signerKey.map(this::createSigner);
        return new Resolved(
                id,
                algorithm,
                signerKey,
                verifierKey,
                signer
        );
    }

    private JWSSigner createSigner(PrivateKey privateKey) {
        try {
            return algorithm.getSignerFactory().create(privateKey);
        } catch (JOSEException e) {
            throw new UncheckedException(e);
        }
    }

    private PublicKey buildVerifierKey(String verifierKey) {
        try {
            return algorithm.getSecurity().decodePublicKey(verifierKey);
        } catch (InvalidKeySpecException e) {
            throw new UncheckedException(e);
        }
    }

    private PrivateKey buildSignerKey(String signerKey) {
        try {
            return algorithm.getSecurity().decodePrivateKey(signerKey);
        } catch (InvalidKeySpecException e) {
            throw new UncheckedException(e);
        }
    }
}
