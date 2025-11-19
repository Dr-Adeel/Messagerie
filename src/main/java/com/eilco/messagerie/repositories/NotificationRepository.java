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

    // Find all notifications for a specific recipient user
    List<Notification> findByRecipientId(Long recipientId);

    // Find all notifications for a specific group
    List<Notification> findByGroupId(Long groupId);

    // Find all unread notifications for a specific user
    // Status = false means unread
    List<Notification> findByRecipientIdAndStatus(Long recipientId, Boolean status);

    // Find all unread notifications for a specific group
    List<Notification> findByGroupIdAndStatus(Long groupId, Boolean status);

    // Find all notifications sent by a specific sender
    List<Notification> findBySenderId(Long senderId);

    // Find all notifications of a specific type
    List<Notification> findByType(String type);

    // Count total unread notifications for a user
    long countByRecipientIdAndStatus(Long recipientId, Boolean status);

    // Count total unread notifications for a group
    long countByGroupIdAndStatus(Long groupId, Boolean status);

    // Custom query to find notifications created within a time range
    @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :startDate AND :endDate ORDER BY n.sentAt DESC")
    List<Notification> findNotificationsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Custom query to find the most recent notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.sentAt DESC LIMIT 10")
    List<Notification> findRecentNotificationsForUser(@Param("recipientId") Long recipientId);
}
