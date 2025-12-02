package com.eilco.messagerie.services;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.implementations.GroupService;
import com.eilco.messagerie.services.interfaces.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final GroupService groupService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService; // Équipe 2
    private final GroupRepository groupRepository;


    public UserService(
            UserRepository userRepository,
            GroupService groupService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthorizationService authorizationService,
            GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
        this.groupRepository = groupRepository;
    }


    // CREATE USER
    @Override
    public UserResponse create(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà.");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //  Cas où l’utilisateur doit être ajouté à un groupe
        if (request.getGroupId() != null) {

            Group group = groupRepository.getById(request.getGroupId());

            //  On récupère l'utilisateur connecté (via Security)
            User currentUser = authorizationService.getCurrentUser();

            //  Vérification de rôle admin dans le groupe
            if (!groupService.isAdminOfGroup(currentUser, group)) {
                throw new RuntimeException("Vous n'êtes pas autorisé à ajouter un membre à ce groupe.");
            }

            user.setGroup(group);
        }

        return userMapper.toResponse(userRepository.save(user));
    }
    @Override
    //  READ (By ID)
    public UserResponse getById(Long id) {

        authorizationService.checkPermission("USER_READ");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return userMapper.toResponse(user);
    }

    //  READ (ALL)
    @Override
    public List<UserResponse> getAll() {

        authorizationService.checkPermission("USER_READ");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    //  UPDATE
    @Override
    public UserResponse update(Long id, UserRequest request) {

        authorizationService.checkPermission("USER_UPDATE");

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

        authorizationService.checkPermission("USER_DELETE");

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        userRepository.deleteById(id);
    }

    //  RECHERCHE PAR USERNAME
    @Override
    public List<UserResponse> searchByUsername(String username) {

        authorizationService.checkPermission("USER_SEARCH");

        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    // RECHERCHE PAR FIRSTNAME
    @Override
    public List<UserResponse> searchByFirstName(String firstname) {

        authorizationService.checkPermission("USER_SEARCH");

        return userRepository.findByFirstNameContainingIgnoreCase(firstname)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

}
