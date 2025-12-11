package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface IUserService extends UserDetailsService {
    UserResponse registerUser(UserRequest userRequest);

    Optional<User> findByUsername(String username);

    List<UserResponse> findAllUsers();


}
