package com.eilco.messagerie.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.JWTService;
import com.eilco.messagerie.services.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private SecurityController controller;

    @Test
    void getToken_delegatesToJwtService() {
        given(jwtService.generateToken(any())).willReturn("jwt-token");
        String token = controller.getToken(new UsernamePasswordAuthenticationToken("user", "pwd"));

        assertThat(token).isEqualTo("jwt-token");
        verify(jwtService).generateToken(any());
    }

    @Test
    void register_returnsCreatedUser() {
        UserResponse response = UserResponse.builder().id(1L).username("alice").build();
        given(userService.create(any(UserRequest.class))).willReturn(response);

        ResponseEntity<UserResponse> entity = controller.register(new UserRequest());

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
    }


    // ---------------------------------------------------------
    // PART 2 — MOCKMVC TESTS (NO @MockBean, no deprecation)
    // ---------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTService jwtServiceMockMvc;

    @Autowired
    private IUserService userServiceMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inject mocks into Spring context safely
    @TestConfiguration
    static class MockConfig {

        @Bean
        JWTService jwtService() {
            return Mockito.mock(JWTService.class);
        }

        @Bean
        IUserService userService() {
            return Mockito.mock(IUserService.class);
        }
    }


    // ------------------------
    // /login OK
    // ------------------------
    @Test
    void login_shouldReturnToken() throws Exception {

        Mockito.when(jwtServiceMockMvc.generateToken(any()))
                .thenReturn("mock-token");

        mockMvc.perform(post("/login")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-token"));
    }

    // ------------------------
    // /login without principal → 401
    // ------------------------
    @Test
    void login_withoutPrincipal_shouldReturn401() throws Exception {
        mockMvc.perform(post("/login"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------
    // /register OK
    // ------------------------
    @Test
    void register_shouldReturn201() throws Exception {

        UserRequest request = UserRequest.builder()
                .username("alice")
                .password("123456")
                .firstName("Alice")
                .lastName("Doe")
                .build();

        UserResponse response = UserResponse.builder()
                .id(100L)
                .username("alice")
                .build();

        Mockito.when(userServiceMockMvc.create(any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // ------------------------
    // /register invalid → 400
    // ------------------------
    @Test
    void register_invalidRequest_shouldReturn400() throws Exception {

        UserRequest bad = UserRequest.builder()
                .username("a")  // invalid
                .password("")   // invalid
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }
}
