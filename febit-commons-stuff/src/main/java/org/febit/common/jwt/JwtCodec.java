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
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.febit.lang.Lazy;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.Maps;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "create")
public class JwtCodec {

    private final JwtCodecProps props;

    private final Lazy<Map<String, JwtKey.Resolved>> keys = Lazy.of(this::mappingKeys);
    private final Lazy<JwtKey.Resolved> signerKey = Lazy.of(this::resolveSingerKey);

    private Map<String, JwtKey.Resolved> mappingKeys() {
        return Maps.mapping(this.props.keys(), JwtKey::id, JwtKey::resolve);
    }

    private JwtKey.Resolved resolveSingerKey() {
        var key = keys.get().get(props.signerKeyId());
        if (key == null) {
            throw new IllegalStateException("No signer key found for key id: " + props.signerKeyId());
        }
        return key;
    }

    protected <T> IResponse<T> onInvalidToken(String message) {
        return IResponse.failed(
                401, "INVALID_TOKEN", message
        );
    }

    protected <T> IResponse<T> onTokenExpired() {
        return IResponse.failed(
                401, "TOKEN_EXPIRED", "token expired"
        );
    }

    @Nullable
    protected JwtKey.Resolved resolveKey(SignedJWT jwt) {
        var id = jwt.getHeader().getKeyID();
        if (id == null) {
            return null;
        }
        return keys.get().get(id);
    }

    public IResponse<JWTClaimsSet> decode(String token) {
        SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            return onInvalidToken("invalid signed JWT format: " + e.getMessage());
        }

        var key = resolveKey(jwt);
        if (key == null) {
            return onInvalidToken("missing key ID");
        }

        JWSVerifier verifier;
        try {
            verifier = key.verifier(jwt.getHeader());
        } catch (JOSEException e) {
            return onInvalidToken("cannot resolve signing verifier: " + e.getMessage());
        }

        try {
            if (!jwt.verify(verifier)) {
                return onInvalidToken("failed pass signing verifier");
            }
        } catch (JOSEException e) {
            return onInvalidToken("failed pass signing verifier: " + e.getMessage());
        }

        JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            return onInvalidToken("cannot get payload from token: " + e.getMessage());
        }

        var now = new Date();

        var expireAt = claims.getExpirationTime();
        if (expireAt == null || expireAt.before(now)) {
            return onTokenExpired();
        }

        var notBefore = claims.getNotBeforeTime();
        if (notBefore != null && notBefore.after(now)) {
            return onInvalidToken("token not effective yet");
        }
        return IResponse.success(claims);
    }

    public String encode(JWTClaimsSet payload) throws JOSEException {
        return encode(payload, header -> {
        });
    }

    public String encode(JWTClaimsSet payload, Consumer<JWSHeader.Builder> headerCustomizer) throws JOSEException {
        var key = signerKey.get();

        var signer = key.signer().orElseThrow(() ->
                new JOSEException("No signer found for key id: " + key.id())
        );

        var header = new JWSHeader.Builder(key.algorithm().getJws())
                .keyID(key.id());
        headerCustomizer.accept(header);

        var jwt = new SignedJWT(
                header.build(),
                payload
        );
        jwt.sign(signer);
        return jwt.serialize();
    }

}
