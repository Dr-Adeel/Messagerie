package com.eilco.messagerie.services.implementations;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;
import com.eilco.messagerie.services.interfaces.IUserService;

import lombok.RequiredArgsConstructor;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final IGroupService groupService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    // CREATE USER
    @Override
    public UserResponse create(UserRequest request) {

        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà.");
        }

        // Mapper la requête vers une entité User
        User user = userMapper.toEntity(request);

        // Encoder le mot de passe fourni par l'utilisateur
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getGroupId() != null) {
            Group group = groupService.getById(request.getGroupId());
            user.setGroup(group);
        }

        // Sauvegarder l'utilisateur sans gestion de groupe ou autorisations
        User savedUser = userRepository.save(user);

        // Retourner la réponse DTO à partir de l'utilisateur sauvegardé
        return userMapper.toResponse(savedUser);
    }
    @Override
    //  READ (By ID)
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return userMapper.toResponse(user);
    }

    //  READ (ALL)
    @Override
    public List<UserResponse> getAll() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    //  UPDATE
    @Override
    public UserResponse update(Long id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

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


    //  DELETE
    @Override
    public void delete(Long id) {


        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        userRepository.deleteById(id);
    }

    //  RECHERCHE PAR USERNAME
    @Override
    public List<UserResponse> searchByUsername(String username) {


        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    // RECHERCHE PAR FIRSTNAME
    @Override
    public List<UserResponse> searchByFirstName(String firstname) {

        return userRepository.findByFirstNameContainingIgnoreCase(firstname)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

}
