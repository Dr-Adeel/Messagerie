package com.eilco.messagerie.services.implementations;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        // Récupération du nom d'utilisateur depuis SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupération de l'utilisateur par nom d'utilisateur (username)
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Current user not found.");
        }
        return user;
    }

}
