package com.eilco.messagerie.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SpringSecurityConfigTest {

    private SpringSecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SpringSecurityConfig();
        // 32-byte secret key required for HS256
        ReflectionTestUtils.setField(config, "jwtKey", "0123456789ABCDEF0123456789ABCDEF");
    }

    @Test
    void jwtEncoder_createsSignedToken() {
        JwtEncoder encoder = config.jwtEncoder();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        assertThat(token).isNotBlank();
    }

    @Test
    void jwtDecoder_verifiesTokenSignedWithConfiguredKey() {
        JwtEncoder encoder = config.jwtEncoder();
        JwtDecoder decoder = config.jwtDecoder();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("bob")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build();

        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        Jwt decoded = decoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo("bob");
    }

    @Test
    void passwordEncoder_hashesAndMatchesPassword() {
        BCryptPasswordEncoder encoder = config.passwordEncoder();

        String raw = "super-secret";
        String hashed = encoder.encode(raw);

        assertThat(hashed).isNotBlank();
        assertThat(encoder.matches(raw, hashed)).isTrue();
    }
}