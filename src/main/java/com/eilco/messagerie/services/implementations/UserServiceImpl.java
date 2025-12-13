package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;
import com.eilco.messagerie.services.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final IGroupService groupService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<UserResponse> findByFirstName(String firstname) {
        return userRepository.findByFirstNameContainingIgnoreCase(firstname)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> findByUserName(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public java.util.List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getGroupId() != null) {
            Group group = groupService.getById(request.getGroupId());
            user.setGroup(group);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }

        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        // Récupération du nom d'utilisateur depuis SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupération de l'utilisateur par nom d'utilisateur (username)
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
