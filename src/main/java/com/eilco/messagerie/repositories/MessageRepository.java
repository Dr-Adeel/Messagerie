package com.eilco.messagerie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eilco.messagerie.repositories.entities.Message;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>  {
    List<Message> findByReceiverGroupIdOrderByTimestampAsc(Long groupId);

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>  {
  
    List<Message> findByReceiverGroupIdOrderByTimestampAsc(Long groupId);
    @Query("""
               SELECT m
               FROM Message m
               WHERE m.receiverGroup IS NULL
                 AND (
                     (m.sender.id = :userAId AND m.receiverUser.id = :userBId)
                  OR (m.sender.id = :userBId AND m.receiverUser.id = :userAId)
                 )
               ORDER BY m.timestamp ASC
               """)
    List<Message> findConversationBetweenUsers(@Param("userAId") Long userAId,
                                               @Param("userBId") Long userBId);
}
