package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * NotificationController - REST API endpoint for notification management
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /api/notifications : Create a new notification
     * This endpoint accepts a NotificationRequest and creates a new notification
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("[NOTIFICATION CONTROLLER] POST /api/notifications - Creating notification from sender: {} to recipient: {}",
                request.getSenderId(), request.getRecipientId());

        // Call service to create notification
        NotificationResponse response = notificationService.createNotification(request);

        log.info("[NOTIFICATION CONTROLLER] Notification created successfully with ID: {}", response.getId());

        // Return 201 Created with the created notification in response body
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/notifications/user/{userId} : Get all notifications for a specific user
     * Retrieves all notifications (read and unread) for the given user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForUser(@PathVariable Long userId) {
        log.info("[NOTIFICATION CONTROLLER] GET /api/notifications/user/{} - Fetching all notifications for user", userId);

        // Call service to retrieve all notifications for user
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);

        log.info("[NOTIFICATION CONTROLLER] Retrieved {} notifications for user: {}", notifications.size(), userId);

        // Return 200 OK with list of notifications
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/user/{userId}/unread
     * Get all unread notifications for a specific user
     * Retrieves only unread notifications (status = false)
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        log.info("[NOTIFICATION CONTROLLER] GET /api/notifications/user/{}/unread - Fetching unread notifications for user", userId);

        // Call service to retrieve unread notifications
        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);

        log.info("[NOTIFICATION CONTROLLER] Retrieved {} unread notifications for user: {}", unreadNotifications.size(), userId);

        // Return 200 OK with list of unread notifications
        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * GET /api/notifications/user/{userId}/unread/count
     * Get count of unread notifications for a user
     * Returns the total number of unread notifications for quick display
     */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCount(@PathVariable Long userId) {
        log.info("[NOTIFICATION CONTROLLER] GET /api/notifications/user/{}/unread/count - Getting unread count for user", userId);

        // Call service to count unread notifications
        long count = notificationService.getUnreadNotificationCount(userId);

        log.info("[NOTIFICATION CONTROLLER] User {} has {} unread notifications", userId, count);

        // Create response map with count
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("unreadCount", count);

        // Return 200 OK with count
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notifications/group/{groupId}
     * Get all notifications for a specific group
     * Retrieves all notifications sent to the given group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForGroup(@PathVariable Long groupId) {
        log.info("[NOTIFICATION CONTROLLER] GET /api/notifications/group/{} - Fetching all notifications for group", groupId);

        // Call service to retrieve group notifications
        List<NotificationResponse> notifications = notificationService.getNotificationsForGroup(groupId);

        log.info("[NOTIFICATION CONTROLLER] Retrieved {} notifications for group: {}", notifications.size(), groupId);

        // Return 200 OK with list of group notifications
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/{notificationId}
     * Get a specific notification by ID
     * Retrieves detailed information about a single notification
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long notificationId) {
        log.info("[NOTIFICATION CONTROLLER] GET /api/notifications/{} - Fetching notification by ID", notificationId);

        // Call service to retrieve notification
        NotificationResponse notification = notificationService.getNotificationById(notificationId);

        log.info("[NOTIFICATION CONTROLLER] Retrieved notification with ID: {}", notificationId);

        // Return 200 OK with the notification
        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/{notificationId}/read
     * Mark a notification as read
     * Updates the notification status to true (read)
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(@PathVariable Long notificationId) {
        log.info("[NOTIFICATION CONTROLLER] PUT /api/notifications/{}/read - Marking notification as read", notificationId);

        // Call service to mark notification as read
        NotificationResponse updatedNotification = notificationService.markNotificationAsRead(notificationId);

        log.info("[NOTIFICATION CONTROLLER] Notification marked as read with ID: {}", notificationId);

        // Return 200 OK with updated notification
        return ResponseEntity.ok(updatedNotification);
    }

    /**
     * PUT /api/notifications/user/{userId}/read-all
     * Mark all unread notifications as read for a user
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead(@PathVariable Long userId) {
        log.info("[NOTIFICATION CONTROLLER] PUT /api/notifications/user/{}/read-all - Marking all notifications as read for user", userId);

        // Call service to mark all notifications as read
        long count = notificationService.markAllNotificationsAsReadForUser(userId);

        log.info("[NOTIFICATION CONTROLLER] {} notifications marked as read for user: {}", count, userId);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("markedAsReadCount", count);
        response.put("message", count + " notifications marked as read");

        // Return 200 OK with count
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/notifications/{notificationId}
     * Delete a notification
     * Removes a notification from the system
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("[NOTIFICATION CONTROLLER] DELETE /api/notifications/{} - Deleting notification", notificationId);

        // Call service to delete notification
        notificationService.deleteNotification(notificationId);

        log.info("[NOTIFICATION CONTROLLER] Notification deleted successfully with ID: {}", notificationId);

        // Return 204 No Content (successful deletion with no response body)
        return ResponseEntity.noContent().build();
    }
}
