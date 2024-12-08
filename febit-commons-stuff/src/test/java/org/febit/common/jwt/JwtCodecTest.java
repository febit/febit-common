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
import com.nimbusds.jose.util.StandardCharset;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;

import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.febit.common.jwt.JwtCodec.CODE_INVALID_TOKEN;
import static org.febit.common.jwt.JwtCodec.CODE_TOKEN_EXPIRED;
import static org.febit.common.jwt.JwtCodec.MSG_INVALID_FORMAT;
import static org.febit.common.jwt.JwtCodec.MSG_INVALID_FORMAT_PAYLOAD;
import static org.febit.common.jwt.JwtCodec.MSG_NO_SINGER_KEY;
import static org.febit.common.jwt.JwtCodec.MSG_NO_VERIFIER_KEY;
import static org.febit.common.jwt.JwtCodec.MSG_TOKEN_EXPIRED;
import static org.febit.common.jwt.JwtCodec.MSG_TOKEN_NOT_EFFECTIVE;
import static org.febit.common.jwt.JwtCodec.MSG_VERIFY_FAILED;
import static org.febit.common.jwt.JwtCodec.STATUS_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class JwtCodecTest {

    static final JwtKey ES_BAD = JwtKey.builder()
            .id("es_bad")
            .algorithm(JwkAlgorithm.ES256)
            .signerKey("MEECAQAwEwY")
            .verifierKey("")
            .build();

    static final JwtKey ES_1 = JwtKey.builder()
            .id("es1")
            .algorithm(JwkAlgorithm.ES256)
            .signerKey("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBSJwcMG9Ih5VeuXTv0yMPLf1B70/kWFe+ZzbZijwoWDA==")
            .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENH7qG/L4uuR9eShmG7+w/0nHT82PPaKUHfbnfp8pjcWiDSdz+Xxqp+Rfb0192HltkZbBzJYpdwzHjB8+UYh2yg==")
            .build();

    static final JwtKey ES_2 = JwtKey.builder()
            .id("es2")
            .algorithm(JwkAlgorithm.ES256)
            .signerKey("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBiXDwPDI8rNUV+m+aqoptpt8NKFGnQ7p8rEysuO0d1Rw==")
            .verifierKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEdamOB+59G8aQv/b3aYcB4m5anFpRj8RbcMz8gOdpq0JWBcAHflhvYs6vSbpsh2GdnN2gE1U68aEZGFBeHhzlvQ==")
            .build();

    static final JwtKey RSA_1 = JwtKey.builder()
            .id("rsa1")
            .algorithm(JwkAlgorithm.RS256)
            .signerKey("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDM7Y+at+NqeepkRYLWtE4UdyTeITdqSTP5Odgm2Vi4j/OAQBpmyTUs7P8+Jcxk2OQmbnWwlipKbU7TwVamli0g266YXzxn7BNvrIkI+35UWShOerVkin2rb/btO+1iTQwtjQUAmCeJ+gcObGu4j9ex1Z/na01Lj2uo1HzlnLFQblTDRHjT/rP7xMjQ7yWq0+VRLVmYj3+CB6+HMWU5blNaolJgpjEexz/sjBadGUNK6Ca3p+wE5NW7ZYdxrobLYEwUhQLmNg4deOKVRTu/blXAQVgxQ7QeTuDfYAHEz8lAL507i1+TxRwqQH1YvR/xDR7kY090HiDGCwZTmqUShrTNAgMBAAECggEAELcj6okrVJ5jKbtTGR5AltdtEMKsLtA6cWQa0c29YziNXR2yyZrfP9iJAqcPTVPfsD7Rp2NoSa/cKSubsh+NHiFX1CKYiRrZhFdndMMcIoHZ5s+IMjjrApaHEVdnsof5q/wqYfUHQfA8rnGA4TS3xvqs01yOQ9E2r+EShaF1WSEGnjMaBYZjCYnC+h05/PkCAH58UdZOix4lNdW3DwNtLDUD/ZS732cYwfU5E6kxSr+ktbwf83M+xCTILBPcqzr4Kc8CjGLmQGsWxyLROThVM6dsepFGUGtVSiDqPv9RnqbD/S3yXgjvfFEbVPsqO9jXUM8yTWhbhwu/ygHZbiQXYQKBgQD2f+xgKWp49OaunwLrWJuakq8zDZ2YMWzanQWmC568r4sLNG9whiGBQUBkKQ262AoGfvlp86LRhkQPsX72o2/hxuK+PeHKqOzb62GrAx6sXCVChWXwxE5Fbdg0RMk7moJg0QSj51PlzQqoRhUgPR7BuaPzLYkEo/MwzsKto2AfaQKBgQDU03jn5qkVV3Fij8alVAII31JK/Kr6Md/zvYqrHEKcEMA0tO9X+8/Elrx1aoCPhe5CME8Yhh0+Ow8H//j/WbagQM6k+cycTnmD8nXl0SoMZQ2yD+cbb0AK4Ckr7QrZiX/WlxkrEJyv1MLqOEHnlQHMlxB+1rq87QzsV4KC2zIhxQKBgQCh9lmksXHA+gEcWWR8qGLNRHYTUG3tEwSX+7Y7bMUm6xysBw7121vmZq5dYVAhJVQpAoL0p92iV9seiJyIh8i6i4huQZC7LRU8RsOMrucXjw/4Tlg1w3Y+TjdR2KR0IkhqPswSuas5gejvueSloyMzXLupy5CM2QCwYBowJvnAiQKBgEoVVuf6je8E4k452B8i0BaAjmfV7jprdBanawbHLpSe7Bksxofs+tTm17FTom7TYy2dHVtfC/zmmAPj4awZcY31ITmiTsw2MOxyUQHvvmBUKOFL0dnl7vgaMZq9x+ITj/GF5VftT22RdUwMyzwuzVKHX1GXs6ciKTZ3+9FLNfc1AoGBAJqeAxYty84XdJUeEAyujWFgg8dHqVZUCQ4wSc2/PRb6cf5rdSYQ8Au/V0ZyQvkKpN9+yHeIpXdys7xLUexLgkVaEnV+deGZywAMYMA/HZSTPZfZ3CzJpc8b6XM5NO0vyQw7CmeV711d0xDOyS/KDrHEHC7JB7Fd/HCn6JewnJgj")
            .verifierKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzO2PmrfjannqZEWC1rROFHck3iE3akkz+TnYJtlYuI/zgEAaZsk1LOz/PiXMZNjkJm51sJYqSm1O08FWppYtINuumF88Z+wTb6yJCPt+VFkoTnq1ZIp9q2/27TvtYk0MLY0FAJgnifoHDmxruI/XsdWf52tNS49rqNR85ZyxUG5Uw0R40/6z+8TI0O8lqtPlUS1ZmI9/ggevhzFlOW5TWqJSYKYxHsc/7IwWnRlDSugmt6fsBOTVu2WHca6Gy2BMFIUC5jYOHXjilUU7v25VwEFYMUO0Hk7g32ABxM/JQC+dO4tfk8UcKkB9WL0f8Q0e5GNPdB4gxgsGU5qlEoa0zQIDAQAB")
            .build();

    static final JwtKey RSA_2 = JwtKey.builder()
            .id("rsa2")
            .algorithm(JwkAlgorithm.RS256)
            .signerKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyUkjluKWXK8+UJU3ib8G/P1Z1m8BwH6K6RYy6ACXSSESblfVIA5RV00TQZ8ecHxBz1c7D4zlqgl956yK8WACvhiZYPnG1Sbjo6plsXoUVHkJQFPl9S/N3eGUrVKWz8kOhH6tSv2N/I4sEKZCYVtLfZpbbtdFTrFoNnn0JNOI+Qf3GZic0zesujlEohB9Ll6/dqWFiJ3XkpJ1yHknmphkHq6/JiGKT0JtZFR+Ssx345GYJijng9HRo+3zJBiYaOOF25EvO/OlhKbiD4tPK+8MSHQzrRfUYMGR9xBnIxNtZcGMnEUKwbnw8hj5mwq13p0skRIrA6sGcGJqS2vwQDu4vAgMBAAECggEADBDiSt0AI2b9AD638urx9rFf9cxIrK2PUsBTAeDxrjViow52Dt3jIx2NCfcDvC4j7Fqax4lwAKN6t2KwMoNX5IiUvqLXCdfHscbyA7Gvmpq2DPomWZKSklJssg5jufXaGqRscMhvgJJUnT78wDobwa8M5Sf71ogSfdpsBnv0FRc/9EMRuzGAENlzJep/LfLgZNUDWRludFSDesqCtUOR55jVzqiFVgnZ+wJhzTACjGQAO7BE2crHkwGyi8niiuhIpHBLxlXLIiqguWw+K2Pg+s+C3Z03j+qzcQocL9XTGRwq2pD/PWpu0I7KPOvjWPmyWJtkymfBrUTbQRLeZUGDHQKBgQDtTqZcmt5CBsSFXTbxv5g06eoj227jufhSacs5ehoPPaEn4NDKR2g0iWP4oPGAa5GnrPMeCneQotGSsQYKfJ5wJ9QCip/4N/+Rh1ILrH/nZiIseJzEP30KuTb49AsuUDDimSbg9vv/hfG1cQ9FtaDutIwoxhans/y9QMAGnQ+7XQKBgQDAXix+7pyLbv8NJrxyBE1lFvA2kDrrmloZBX8NyA+BtkFK48MrgSHxKtNPUmfkBCfmwtt8lqHB7aKB832e7sBs7/y7iMwLK4ua/YyBIiWW4GYKcEoK/05j+NJyqksvpvLlzD4obuisoQMn56Lv3MA6/mJQf9zXqTYDtvN5S6GC+wKBgQCHLYourjl1Ow2vOo4V36IAYk7x5gmgx0rhB0qwjxFRJTYr1TCaSVresnS0KFnpYV0zOp2yuxuo1Fa1TQm3540JLmbim7zMVHnMmFzwMW8ajV9iHcKPN+sutJrP7ZSA6UUvjzZKZ+bcg5xQUvr0JqZp04cTouM83JZX6A56tfVEMQKBgCUdBjCnEITGMWb0Jj9JHTV3q3ibcwKpkOBq8wxRoMxBwW8Vaj4md8nznTkciPaw5pHa3kW3OhYUJtozg/T50x4xXu0/gz5UdGa+rDDtyZrGSGmTKHXes90N0GxW9pKG/y3erc9XNoS3u8gjJIhqapv3IDivsofZGZa14VtxgAOpAoGARGWphC+33aPmniCBeywy/rg5d8D+ZbteEg1yQdhFEeW1DZY2XF8meZmvE353ZQ+eSCvhxlVgbpV5mOWYnvKjiBqb7yycAdUtZBM/Hf0XJnrKpasn77GOF8n0bEaZzpk9e3BKP83WGowcAVtzoR63T3uYhs6/BBDUgwVwwcXkumQ=")
            .verifierKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAslJI5billyvPlCVN4m/Bvz9WdZvAcB+iukWMugAl0khEm5X1SAOUVdNE0GfHnB8Qc9XOw+M5aoJfeesivFgAr4YmWD5xtUm46OqZbF6FFR5CUBT5fUvzd3hlK1Sls/JDoR+rUr9jfyOLBCmQmFbS32aW27XRU6xaDZ59CTTiPkH9xmYnNM3rLo5RKIQfS5ev3alhYid15KSdch5J5qYZB6uvyYhik9CbWRUfkrMd+ORmCYo54PR0aPt8yQYmGjjhduRLzvzpYSm4g+LTyvvDEh0M60X1GDBkfcQZyMTbWXBjJxFCsG58PIY+ZsKtd6dLJESKwOrBnBiaktr8EA7uLwIDAQAB")
            .build();

    static final JwtCodecProps PROPS = JwtCodecProps.builder()
            .signerKeyId("es1")
            .keys(List.of(ES_1, ES_2, RSA_1, RSA_2))
            .build();

    static final JwtCodec CODEC_ALL = new JwtCodec(PROPS);

    static final JWTClaimsSet PL_X = new JWTClaimsSet.Builder()
            .subject("x")
            .build();

    @Test
    void basic() {
        var token = assertDoesNotThrow(() -> CODEC_ALL.encode(PL_X));
        var decoded = assertDoesNotThrow(() -> CODEC_ALL.decode(token));

        assertTrue(decoded.isSuccess());
        assertEquals(PL_X, decoded.getData());
    }

    @Test
    void expired() throws JOSEException {
        var codec = spy(CODEC_ALL);

        var now = Instant.parse("2024-10-01T10:00:00Z");
        var expiresAt = now.plus(12, ChronoUnit.HOURS);
        var effectiveAfter = now.plus(1, ChronoUnit.HOURS);

        var token = codec.encode(new JWTClaimsSet.Builder()
                .subject("x")
                .expirationTime(Date.from(expiresAt))
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(effectiveAfter))
                .build());
        when(codec.now()).thenReturn(now);

        var decoded = codec.decode(token);
        assertFalse(decoded.isSuccess());
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertEquals(MSG_TOKEN_NOT_EFFECTIVE, decoded.getMessage());

        when(codec.now()).thenReturn(now.plus(30, ChronoUnit.MINUTES));
        decoded = codec.decode(token);
        assertFalse(decoded.isSuccess());
        assertEquals(MSG_TOKEN_NOT_EFFECTIVE, decoded.getMessage());

        when(codec.now()).thenReturn(effectiveAfter);
        decoded = codec.decode(token);
        assertTrue(decoded.isSuccess());

        when(codec.now()).thenReturn(effectiveAfter.plus(1, ChronoUnit.HOURS));
        decoded = codec.decode(token);
        assertTrue(decoded.isSuccess());

        when(codec.now()).thenReturn(expiresAt);
        decoded = codec.decode(token);
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_TOKEN_EXPIRED, decoded.getCode());
        assertEquals(MSG_TOKEN_EXPIRED, decoded.getMessage());

        when(codec.now()).thenReturn(expiresAt.plus(1, ChronoUnit.HOURS));
        decoded = codec.decode(token);
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_TOKEN_EXPIRED, decoded.getCode());
        assertEquals(MSG_TOKEN_EXPIRED, decoded.getMessage());
    }

    @Test
    void invalidKey() throws JOSEException {
        var codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId(ES_BAD.id())
                .key(ES_BAD)
                .build()
        );

        var ex = assertThrows(RuntimeException.class, () -> codec.encode(PL_X));
        assertInstanceOf(InvalidKeySpecException.class, ex.getCause());

        var token = CODEC_ALL.encode(PL_X);
        ex = assertThrows(RuntimeException.class, () -> codec.decode(token));
        assertInstanceOf(InvalidKeySpecException.class, ex.getCause());
    }

    @Test
    void wrongKey() throws JOSEException {
        var codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId("x")
                .key(JwtKey.builder()
                        .id("x")
                        .algorithm(JwkAlgorithm.ES256)
                        .signerKey(ES_1.signerKey())
                        .verifierKey(ES_2.verifierKey())
                        .build())
                .build()
        );
        var token = codec.encode(PL_X);
        var decoded = assertDoesNotThrow(() -> codec.decode(token));
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertEquals(MSG_VERIFY_FAILED, decoded.getMessage());
    }

    @Test
    void invalidFormat() {
        var decoded = CODEC_ALL.decode("");
        assertFalse(decoded.isSuccess());
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertTrue(decoded.getMessage().startsWith(MSG_INVALID_FORMAT));
    }

    @Test
    void missingSignerKey() {
        var codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId(ES_1.id())
                .keys(List.of(ES_2))
                .build());

        var ex = assertThrows(IllegalStateException.class, () -> codec.encode(PL_X));
        assertTrue(ex.getMessage().startsWith(MSG_NO_SINGER_KEY));
    }

    @Test
    void invalidPayload() throws JOSEException {
        var key = ES_1.resolve();
        var header = new JWSHeader.Builder(key.algorithm().getJws())
                .keyID(key.id())
                .build();

        var token = header.toBase64URL().toString()
                + ".INVALID";

        var signer = key.signer().orElseThrow();
        token += "." + signer.sign(header, token.getBytes(StandardCharset.UTF_8));

        var decoded = CODEC_ALL.decode(token);
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertTrue(decoded.getMessage().startsWith(MSG_INVALID_FORMAT_PAYLOAD));
    }

    @Test
    void missingVerifierKey() throws JOSEException {
        var codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId(ES_2.id())
                .keys(List.of(ES_2))
                .build());

        var token = CODEC_ALL.encode(PL_X);
        var decoded = codec.decode(token);
        assertFalse(decoded.isSuccess());

        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertEquals(MSG_NO_VERIFIER_KEY, decoded.getMessage());
    }

    @Test
    void notVerifierKeyConfig() throws JOSEException {
        var codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId("x")
                .key(JwtKey.builder()
                        .id("x")
                        .algorithm(JwkAlgorithm.ES256)
                        .signerKey(ES_1.signerKey())
                        .build())
                .build()
        );
        var token = codec.encode(PL_X);
        var decoded = assertDoesNotThrow(() -> codec.decode(token));
        assertEquals(STATUS_UNAUTHORIZED, decoded.getStatus());
        assertEquals(CODE_INVALID_TOKEN, decoded.getCode());
        assertEquals(MSG_NO_VERIFIER_KEY, decoded.getMessage());
    }

    @Test
    void diffConfig() throws JOSEException {
        var es1Codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId(ES_1.id())
                .keys(List.of(ES_1, ES_2))
                .build());
        var es2Codec = new JwtCodec(JwtCodecProps.builder()
                .signerKeyId(ES_2.id())
                .keys(List.of(ES_1, ES_2))
                .build());

        var token = es2Codec.encode(PL_X);

        var result = es1Codec.decode(token);
        assertTrue(result.isSuccess());
        assertEquals(PL_X, result.getData());

        result = es2Codec.decode(token);
        assertTrue(result.isSuccess());
        assertEquals(PL_X, result.getData());

        result = CODEC_ALL.decode(token);
        assertTrue(result.isSuccess());
        assertEquals(PL_X, result.getData());
    }
}
