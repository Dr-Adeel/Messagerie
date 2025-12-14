package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.services.interfaces.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;
    private final IUserService userService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("[CONTROLLER] Creating notification from {} to {}", request.getSenderId(), request.getRecipientId());
        NotificationResponse response = notificationService.createNotification(request);
        log.info("[CONTROLLER] Notification created with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        log.info("[CONTROLLER] Fetching unread notifications for user: {}", username);

        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);
        log.info("[CONTROLLER] Retrieved {} unread notifications for user {}", unreadNotifications.size(), username);
        return ResponseEntity.ok(unreadNotifications);
    }


    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCount(Authentication authentication) {
        String username = authentication.getName();
        log.info("[CONTROLLER] Counting unread notifications for user: {}", username);

        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        long count = notificationService.getUnreadNotificationCount(userId);
        log.info("[CONTROLLER] User {} has {} unread notifications", username, count);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("userId", userId);
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForUser(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);
        log.info("[CONTROLLER] Retrieved {} notifications for user {}", notifications.size(), userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);
        log.info("[CONTROLLER] Retrieved {} unread notifications for user {}", unreadNotifications.size(), userId);
        return ResponseEntity.ok(unreadNotifications);
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCountForUser(@PathVariable Long userId) {
        long count = notificationService.getUnreadNotificationCount(userId);
        log.info("[CONTROLLER] User {} has {} unread notifications", userId, count);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForGroup(@PathVariable Long groupId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsForGroup(groupId);
        log.info("[CONTROLLER] Retrieved {} notifications for group {}", notifications.size(), groupId);
        return ResponseEntity.ok(notifications);
    }

    // ⚠️ Cette route doit venir APRÈS toutes les routes spécifiques
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long notificationId) {
        NotificationResponse notification = notificationService.getNotificationById(notificationId);
        log.info("[CONTROLLER] Retrieved notification with ID: {}", notificationId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(@PathVariable Long notificationId) {
        NotificationResponse updatedNotification = notificationService.markNotificationAsRead(notificationId);
        log.info("[CONTROLLER] Notification marked as read with ID: {}", notificationId);
        return ResponseEntity.ok(updatedNotification);
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead(@PathVariable Long userId) {
        long count = notificationService.markAllNotificationsAsReadForUser(userId);
        log.info("[CONTROLLER] {} notifications marked as read for user {}", count, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("markedAsReadCount", count);
        response.put("message", count + " notifications marked as read");
        return ResponseEntity.ok(response);
    }

    // ✅ NOUVELLE ROUTE : Marquer toutes les notifications comme lues pour l'utilisateur connecté
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllMyNotificationsAsRead(Authentication authentication) {
        String username = authentication.getName();
        log.info("[CONTROLLER] Marking all notifications as read for user: {}", username);

        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        long count = notificationService.markAllNotificationsAsReadForUser(userId);
        log.info("[CONTROLLER] {} notifications marked as read for user {}", count, username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("userId", userId);
        response.put("markedAsReadCount", count);
        response.put("message", count + " notifications marked as read");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        log.info("[CONTROLLER] Notification deleted with ID: {}", notificationId);
        return ResponseEntity.noContent().build();
    }
}