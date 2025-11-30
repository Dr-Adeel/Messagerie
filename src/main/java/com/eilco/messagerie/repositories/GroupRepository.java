package com.eilco.messagerie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eilco.messagerie.repositories.entities.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{

}
