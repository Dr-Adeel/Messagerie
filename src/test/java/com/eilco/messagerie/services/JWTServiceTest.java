package com.eilco.messagerie.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JWTServiceTest {

    @Test
    void generateToken_containsSubjectAndExpiration() {
        byte[] secret = "test-secret-key-1234567890".getBytes(StandardCharsets.UTF_8);
        JWTService jwtService = new JWTService(new NimbusJwtEncoder(new ImmutableSecret<>(secret)));

        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", "pwd");

        String token = jwtService.generateToken(authentication);

        SecretKeySpec secretKey = new SecretKeySpec(secret, "HmacSHA256");
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        Jwt decoded = decoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("alice");
        assertThat(decoded.getExpiresAt()).isNotNull();
    }
}
