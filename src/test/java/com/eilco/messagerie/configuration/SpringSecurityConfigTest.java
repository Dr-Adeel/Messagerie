package com.eilco.messagerie.configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private HttpSecurity httpSecurity;
    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private DaoAuthenticationConfigurer<AuthenticationManagerBuilder, CustomUserDetailsService> daoAuthenticationConfigurer;
    @Mock
    private AuthenticationManager builtAuthenticationManager;

    @BeforeEach
    @SuppressWarnings("unused")
    void init() {
        config = new SpringSecurityConfig();
        // 32-byte secret key required for HS256
        ReflectionTestUtils.setField(config, "jwtKey", "0123456789ABCDEF0123456789ABCDEF");
        ReflectionTestUtils.setField(config, "customUserDetailsService", customUserDetailsService);
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

    @Test
    void authenticationManager_usesCustomUserDetailsService() throws Exception {
        BCryptPasswordEncoder passwordEncoder = config.passwordEncoder();
        when(httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)).thenReturn(authenticationManagerBuilder);
        when(authenticationManagerBuilder.userDetailsService(customUserDetailsService)).thenReturn(daoAuthenticationConfigurer);
        when(daoAuthenticationConfigurer.passwordEncoder(passwordEncoder)).thenReturn(daoAuthenticationConfigurer);
        when(authenticationManagerBuilder.build()).thenReturn(builtAuthenticationManager);

        AuthenticationManager manager = config.authenticationManager(httpSecurity, passwordEncoder);

        assertThat(manager).isSameAs(builtAuthenticationManager);
        verify(authenticationManagerBuilder).userDetailsService(customUserDetailsService);
        verify(daoAuthenticationConfigurer).passwordEncoder(passwordEncoder);
    }
}