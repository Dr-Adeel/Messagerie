package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        // Récupération du nom d'utilisateur depuis SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupération de l'utilisateur par nom d'utilisateur (username)
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
