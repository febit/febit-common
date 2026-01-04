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
import org.febit.lang.Lazy;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.Maps;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

public class JwtCodec {

    static final int STATUS_UNAUTHORIZED = 401;
    static final String CODE_INVALID_TOKEN = "INVALID_TOKEN";
    static final String CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";

    static final String MSG_INVALID_FORMAT = "invalid JWT format";
    static final String MSG_INVALID_FORMAT_PAYLOAD = MSG_INVALID_FORMAT + ", failed to parse payload";
    static final String MSG_TOKEN_EXPIRED = "token expired";
    static final String MSG_TOKEN_NOT_EFFECTIVE = "token not effective yet";
    static final String MSG_NO_VERIFIER_KEY = "cannot found verifier key for token";
    static final String MSG_NO_SINGER_KEY = "no signer key found";
    static final String MSG_VERIFY_FAILED = "failed pass signing verifier";

    protected final Lazy<Map<String, JwtKey.Resolved>> keys = Lazy.of(this::mappingKeys);
    protected final Lazy<JwtKey.Resolved> signerKey = Lazy.of(this::resolveSingerKey);

    protected final JwtCodecProps props;

    public JwtCodec(JwtCodecProps props) {
        this.props = props;
    }

    private Map<String, JwtKey.Resolved> mappingKeys() {
        return Maps.mapping(this.props.keys(), JwtKey::id, JwtKey::resolve);
    }

    private JwtKey.Resolved resolveSingerKey() {
        var key = keys.get().get(props.signerKeyId());
        if (key == null) {
            throw new IllegalStateException(MSG_NO_SINGER_KEY + ", for id: " + props.signerKeyId());
        }
        return key;
    }

    protected <T> IResponse<T> onInvalidToken(String message) {
        return IResponse.failed(STATUS_UNAUTHORIZED, CODE_INVALID_TOKEN, message);
    }

    protected <T> IResponse<T> onTokenExpired() {
        return IResponse.failed(STATUS_UNAUTHORIZED, CODE_TOKEN_EXPIRED, MSG_TOKEN_EXPIRED);
    }

    protected Instant now() {
        return Instant.now();
    }

    protected JwtKey.@Nullable Resolved resolveKey(SignedJWT jwt) {
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
            return onInvalidToken(MSG_INVALID_FORMAT + ": " + e.getMessage());
        }

        var key = resolveKey(jwt);
        if (key == null || key.verifierKey().isEmpty()) {
            return onInvalidToken(MSG_NO_VERIFIER_KEY);
        }

        JWSVerifier verifier;
        try {
            verifier = key.verifier(jwt.getHeader());
        } catch (JOSEException e) {
            return onInvalidToken("cannot resolve signing verifier: " + e.getMessage());
        }

        try {
            if (!jwt.verify(verifier)) {
                return onInvalidToken(MSG_VERIFY_FAILED);
            }
        } catch (JOSEException e) {
            return onInvalidToken(MSG_VERIFY_FAILED + ": " + e.getMessage());
        }

        JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            return onInvalidToken(MSG_INVALID_FORMAT_PAYLOAD + ": " + e.getMessage());
        }

        var now = now();
        var expireAt = claims.getExpirationTime();
        if (expireAt != null && expireAt.toInstant().compareTo(now) <= 0) {
            return onTokenExpired();
        }

        var notBefore = claims.getNotBeforeTime();
        if (notBefore != null && notBefore.toInstant().isAfter(now)) {
            return onInvalidToken(MSG_TOKEN_NOT_EFFECTIVE);
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
                new JOSEException(MSG_NO_SINGER_KEY + ", id: " + key.id())
        );

        var header = new JWSHeader.Builder(key.algorithm().getJws());
        headerCustomizer.accept(header);
        header.keyID(key.id());

        var jwt = new SignedJWT(
                header.build(),
                payload
        );
        jwt.sign(signer);
        return jwt.serialize();
    }

}
