package com.eilco.messagerie;

import com.eilco.messagerie.services.JWTService;
import com.eilco.messagerie.services.interfaces.IMessageService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SendMessageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder encoder;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {

        @Bean
        JwtEncoder jwtEncoder() {
            return Mockito.mock(JwtEncoder.class);
        }

        @Bean
        IMessageService messageService() {
            return Mockito.mock(IMessageService.class);
        }

        @Bean
        JWTService jwtService(JwtEncoder encoder) {
            return new JWTService(encoder);
        }
    }

    @Test
    void testSendMessageWithValidToken() throws Exception {

        Jwt fakeJwt = new Jwt(
                "fake.jwt.token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "testUser")
        );

        Mockito.when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(fakeJwt);

        //Mockito.when(messageService.sendMessage(any())).thenReturn(true);

        String jsonPayload = """
            {
                "content": "Hello world",
                "groupId": 1
            }
        """;

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", "Bearer fake.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());
    }
}
