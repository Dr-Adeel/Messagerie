package com.eilco.messagerie.repositories;

import com.eilco.messagerie.repositories.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationRepository - Data access layer for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findByGroupId(Long groupId);

    List<Notification> findByRecipientIdAndStatus(Long recipientId, Boolean status);

    List<Notification> findByGroupIdAndStatus(Long groupId, Boolean status);

    List<Notification> findBySenderId(Long senderId);

    List<Notification> findByType(String type);

    long countByRecipientIdAndStatus(Long recipientId, Boolean status);

    long countByGroupIdAndStatus(Long groupId, Boolean status);

    @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :startDate AND :endDate ORDER BY n.sentAt DESC")
    List<Notification> findNotificationsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.sentAt DESC LIMIT 10")
    List<Notification> findRecentNotificationsForUser(@Param("recipientId") Long recipientId);
}
