package com.eilco.messagerie.repositories;

import com.eilco.messagerie.repositories.entities.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    List<MessageStatus> findByReceiverIdAndIsReadFalse(Long receiverId);
}
