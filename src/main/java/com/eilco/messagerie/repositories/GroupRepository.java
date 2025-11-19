package com.eilco.messagerie.repositories;

import com.eilco.messagerie.repositories.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
