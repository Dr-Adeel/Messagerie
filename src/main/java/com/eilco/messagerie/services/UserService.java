package com.eilco.messagerie.services;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserResponse saveUser(UserRequest request) {
        User entity = userMapper.toEntity(request);

        entity.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toResponse(userRepository.save(entity));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> {
                    return new RuntimeException("User not found");
                });

        existing.setFirstName(request.getFirstName() != null ? request.getFirstName() : existing.getFirstName());
        existing.setLastName(request.getLastName() != null ? request.getLastName() : existing.getLastName());
        existing.setUsername(request.getUsername() != null ? request.getUsername() : existing.getUsername());

        return userMapper.toResponse(userRepository.save(existing));

    }
}
