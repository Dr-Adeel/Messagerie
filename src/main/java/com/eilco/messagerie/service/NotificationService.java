package com.eilco.messagerie.service;

import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.NotificationRepository;
import com.eilco.messagerie.repositories.entities.Notification;
import com.eilco.messagerie.mappers.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationService - Business logic layer for notification management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    // Injecting the NotificationRepository for database operations
    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;

    /**
     * Create a new notification
     * This method receives a NotificationRequest, validates it,
     * creates a Notification entity, saves it to database, and returns a response
     *
     * @param request - NotificationRequest containing notification details
     * @return NotificationResponse - the created notification with all details
     */
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("[NOTIFICATION SERVICE] Creating new notification - Sender: {}, Recipient: {}, Type: {}",
                request.getSenderId(), request.getRecipientId(), request.getType());

        // Validate that at least a recipient is provided
        if (request.getRecipientId() == null) {
            log.warn("[NOTIFICATION SERVICE] RecipientId is required to create notification");
            throw new IllegalArgumentException("Recipient ID is required for creating a notification");
        }

        Notification notification = notificationMapper.toEntity(request);

        // Set timestamps if not provided in request
        if (notification.getSentAt() == null) {
            notification.setSentAt(LocalDateTime.now());
        }
        if (notification.getStatus() == null) {
            notification.setStatus(false); // Default to unread (false)
        }
        notification.setSentAt(LocalDateTime.now());

        // Save the notification to the database
        Notification savedNotification = notificationRepository.save(notification);

        log.info("[NOTIFICATION SERVICE] Notification created successfully with ID: {}", savedNotification.getId());

        return notificationMapper.toResponse(savedNotification);
    }

    /**
     * Get all notifications for a specific recipient user
     * Used to retrieve all notifications (read and unread) for a user
     */
    public List<NotificationResponse> getNotificationsForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Fetching all notifications for user: {}", recipientId);

        // Query database for all notifications for this user
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);

        log.info("[NOTIFICATION SERVICE] Found {} notifications for user: {}", notifications.size(), recipientId);

        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all unread notifications for a specific user
     * Filters notifications where status = false (unread)
     */
    public List<NotificationResponse> getUnreadNotificationsForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Fetching unread notifications for user: {}", recipientId);

        // Query database for unread notifications (status = false)
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndStatus(recipientId, false);

        log.info("[NOTIFICATION SERVICE] Found {} unread notifications for user: {}", unreadNotifications.size(), recipientId);

        return unreadNotifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all notifications for a specific group
     * Used to retrieve notifications sent to a group
     */
    public List<NotificationResponse> getNotificationsForGroup(Long groupId) {
        log.info("[NOTIFICATION SERVICE] Fetching all notifications for group: {}", groupId);

        // Query database for all notifications for this group
        List<Notification> notifications = notificationRepository.findByGroupId(groupId);

        log.info("[NOTIFICATION SERVICE] Found {} notifications for group: {}", notifications.size(), groupId);

        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific notification by ID
     * Retrieves a single notification with all its details
     */
    public NotificationResponse getNotificationById(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Fetching notification with ID: {}", notificationId);

        // Query database for the notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("[NOTIFICATION SERVICE] Notification not found with ID: {}", notificationId);
                    return new RuntimeException("Notification not found with ID: " + notificationId);
                });

        log.info("[NOTIFICATION SERVICE] Notification retrieved successfully with ID: {}", notificationId);

        return notificationMapper.toResponse(notification);
    }

    /**
     * Mark a notification as read
     * Updates the status field to true (read) and saves to database
     */
    @Transactional
    public NotificationResponse markNotificationAsRead(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Marking notification as read with ID: {}", notificationId);

        // Retrieve the notification from database
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("[NOTIFICATION SERVICE] Notification not found with ID: {}", notificationId);
                    return new RuntimeException("Notification not found with ID: " + notificationId);
                });

        notification.setStatus(true);

        // Save the updated notification
        Notification updatedNotification = notificationRepository.save(notification);

        log.info("[NOTIFICATION SERVICE] Notification marked as read with ID: {}", notificationId);

        return notificationMapper.toResponse(updatedNotification);
    }

    /**
     * Mark all unread notifications as read for a specific user
     */
    @Transactional
    public long markAllNotificationsAsReadForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Marking all unread notifications as read for user: {}", recipientId);

        // Retrieve all unread notifications for the user
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndStatus(recipientId, false);

        log.info("[NOTIFICATION SERVICE] Found {} unread notifications to mark as read", unreadNotifications.size());

        // Update each notification's status to read
        unreadNotifications.forEach(notification -> notification.setStatus(true));

        // Save all updated notifications
        notificationRepository.saveAll(unreadNotifications);

        log.info("[NOTIFICATION SERVICE] {} notifications marked as read for user: {}", unreadNotifications.size(), recipientId);

        // Return the count of notifications marked as read
        return unreadNotifications.size();
    }

    /**
     * Get count of unread notifications for a user
     * Returns the total number of unread notifications
     */
    public long getUnreadNotificationCount(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Counting unread notifications for user: {}", recipientId);

        // Query database for count of unread notifications
        long count = notificationRepository.countByRecipientIdAndStatus(recipientId, false);

        log.info("[NOTIFICATION SERVICE] User {} has {} unread notifications", recipientId, count);

        return count;
    }

    /**
     * Delete a notification
     * Removes a notification from the database
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Deleting notification with ID: {}", notificationId);

        // Check if notification exists
        if (!notificationRepository.existsById(notificationId)) {
            log.error("[NOTIFICATION SERVICE] Notification not found with ID: {}", notificationId);
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }

        // Delete the notification
        notificationRepository.deleteById(notificationId);

        log.info("[NOTIFICATION SERVICE] Notification deleted successfully with ID: {}", notificationId);
    }

}
