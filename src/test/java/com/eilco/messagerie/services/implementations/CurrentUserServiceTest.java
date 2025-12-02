package com.eilco.messagerie.services.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private SecurityContext securityContext;

    @BeforeEach
    void setUpContext() {
        securityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsUserFromRepository() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john", "password");
        securityContext.setAuthentication(authentication);

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        given(userRepository.findByUsername("john")).willReturn(user);

        User result = currentUserService.getCurrentUser();

        assertThat(result).isEqualTo(user);
        verify(userRepository).findByUsername("john");
    }

    @Test
    void getCurrentUser_throwsWhenUserNotFound() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("missing", "password");
        securityContext.setAuthentication(authentication);
        given(userRepository.findByUsername("missing")).willReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> currentUserService.getCurrentUser());
        assertThat(exception.getMessage()).contains("courant introuvable");
    }
}
