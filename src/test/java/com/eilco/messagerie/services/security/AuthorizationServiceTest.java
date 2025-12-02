package com.eilco.messagerie.services.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @AfterEach
    void cleanContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsAuthenticatedUser() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("alice", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User();
        user.setUsername("alice");
        given(userRepository.findByUsername("alice")).willReturn(user);

        User result = authorizationService.getCurrentUser();

        assertEquals("alice", result.getUsername());
    }

    @Test
    void getCurrentUser_throwsWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(RuntimeException.class, authorizationService::getCurrentUser);
    }

    @Test
    void checkPermission_allowsWhenPermissionEmpty() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("bob", "pwd", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> authorizationService.checkPermission(""));
    }

    @Test
    void checkPermission_allowsWhenAuthorityMatchesOrAdmin() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("carol", "pwd",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("USER_READ")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> authorizationService.checkPermission("USER_READ"));
    }

    @Test
    void checkPermission_adminBypassesSpecificPermission() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("eve", "pwd",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> authorizationService.checkPermission("ANY_PERMISSION"));
    }

    @Test
    void checkPermission_deniesWhenAuthorityMissing() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("dan", "pwd",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(RuntimeException.class, () -> authorizationService.checkPermission("USER_DELETE"));
    }
}
