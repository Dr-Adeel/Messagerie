package com.eilco.messagerie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eilco.messagerie.repositories.entities.Message;

public interface MessageRepository extends JpaRepository<Message, Long>  {

}
