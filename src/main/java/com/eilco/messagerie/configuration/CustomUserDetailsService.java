package com.eilco.messagerie.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private com.eilco.messagerie.repositories.UserRepository userRepository; // FULL NAME

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // FULLY QUALIFIED NAME FOR ENTITY
        com.eilco.messagerie.repositories.entities.User dbUser =
                userRepository.findByUsername(username);

        if (dbUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new User(
                dbUser.getUsername(),
                dbUser.getPassword(),
                getGrantedAuthorities()
        );
    }

    private List<GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + "USER"));
        return authorities;
    }
}
