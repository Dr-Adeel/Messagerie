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
}
