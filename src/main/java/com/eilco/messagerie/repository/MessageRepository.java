package com.eilco.messagerie.repository;

import com.eilco.messagerie.entitty.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverGroupIdOrderByTimestampAsc(Long groupId);
}
