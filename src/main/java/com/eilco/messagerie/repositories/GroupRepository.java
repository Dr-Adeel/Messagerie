package com.eilco.messagerie.repositories;

import com.eilco.messagerie.repositories.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
      Optional<Group> findByName(String name);
}
