package com.eilco.messagerie.services.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Utilisateur courant introuvable");
        }
        return user;
    }

    public void checkPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        if (permission == null || permission.isBlank()) {
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            // Aucun rôle explicite : on autorise par défaut.
            return;
        }

        boolean allowed = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> permission.equalsIgnoreCase(auth) || "ROLE_ADMIN".equalsIgnoreCase(auth));

        if (!allowed) {
            throw new RuntimeException("Permission refusée : " + permission);
        }
    }
}
