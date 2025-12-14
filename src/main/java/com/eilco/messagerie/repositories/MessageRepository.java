package com.eilco.messagerie.repositories;




import com.eilco.messagerie.repositories.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
      List<Message> findByReceiverGroupIdOrderByTimestampAsc(Long groupId);

      @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId1 AND m.receiverUser.id = :userId2) OR (m.sender.id = :userId2 AND m.receiverUser.id = :userId1) ORDER BY m.timestamp ASC")
      List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}

