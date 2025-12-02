package com.eilco.messagerie;

import com.eilco.messagerie.services.JWTService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @TestConfiguration
    static class MockConfig {

        @Bean
        JwtEncoder jwtEncoder() {
            return Mockito.mock(JwtEncoder.class);
        }

        @Bean
        JWTService jwtService(JwtEncoder encoder) {
            return new JWTService(encoder);
        }
    }

    @Test
    void generateToken_shouldUseJwtEncoder() {

        Jwt fakeJwt = new Jwt(
                "fake-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "john")
        );

        Mockito.when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(fakeJwt);

        String token = jwtService.generateToken(
                new UsernamePasswordAuthenticationToken("john", "pwd")
        );

        assertThat(token).isEqualTo("fake-token-value");
        Mockito.verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }
}
