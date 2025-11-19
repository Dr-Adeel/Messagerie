package com.eilco.messagerie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eilco.messagerie.repositories.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
