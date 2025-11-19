package com.eilco.messagerie.repositories;

import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findAllByGroup(Group group);

}
