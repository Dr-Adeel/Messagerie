package com.eilco.messagerie.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.implementations.JWTService;
import com.eilco.messagerie.services.interfaces.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private IUserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        AuthController controller = new AuthController(jwtService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void register_returnsCreatedUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("neo")
                .password("secret1")
                .firstName("Neo")
                .lastName("Matrix")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("neo")
                .build();

        given(userService.create(any(UserRequest.class))).willReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("neo"));

        verify(userService).create(any(UserRequest.class));
    }

    @Test
    void login_returnsJwtToken() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("neo", "secret");
        given(jwtService.generateToken(authentication)).willReturn("token123");

        mockMvc.perform(post("/auth/login")
                        .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(content().string("token123"));

        verify(jwtService).generateToken(authentication);
    }
}
