package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.exceptions.InvalidNotificationRequestException;
import com.eilco.messagerie.exceptions.NotificationNotFoundException;
import com.eilco.messagerie.mappers.NotificationMapper;
import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.NotificationRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Notification;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("[NOTIFICATION SERVICE] Creating notification - sender={}, recipient={}, type={}",
                request.getSenderId(), request.getRecipientId(), request.getType());

        if (request.getRecipientId() == null) {
            log.warn("[NOTIFICATION SERVICE] Missing recipientId");
            throw new InvalidNotificationRequestException("Recipient ID is required for creating a notification");
        }

        Notification notification = notificationMapper.toEntity(request);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(false); // unread

        Notification saved = notificationRepository.save(notification);

        log.info("[NOTIFICATION SERVICE] Notification created with ID={}", saved.getId());

        NotificationResponse response = notificationMapper.toResponse(saved);

        // 🔔 ENVOI VIA WEBSOCKET
        // Récupérer le username du destinataire
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        log.info("[NOTIFICATION SERVICE] Sending notification via WebSocket to user: {}", recipient.getUsername());

        // Envoyer à l'utilisateur spécifique via /user/{username}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/notifications",
                response
        );

        return response;
    }

    @Override
    public List<NotificationResponse> getNotificationsForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Fetching notifications for user {}", recipientId);

        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotificationsForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Fetching unread notifications for user {}", recipientId);

        return notificationRepository.findByRecipientIdAndStatus(recipientId, false)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getNotificationsForGroup(Long groupId) {
        log.info("[NOTIFICATION SERVICE] Fetching notifications for group {}", groupId);

        return notificationRepository.findByGroupId(groupId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse getNotificationById(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Fetching notification {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse markNotificationAsRead(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Marking notification {} as read", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.setStatus(true);

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public long markAllNotificationsAsReadForUser(Long recipientId) {
        log.info("[NOTIFICATION SERVICE] Marking all notifications as read for user {}", recipientId);

        List<Notification> unread = notificationRepository.findByRecipientIdAndStatus(recipientId, false);
        unread.forEach(n -> n.setStatus(true));
        notificationRepository.saveAll(unread);

        return unread.size();
    }

    @Override
    public long getUnreadNotificationCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndStatus(recipientId, false);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        log.info("[NOTIFICATION SERVICE] Deleting notification {}", notificationId);

        if (!notificationRepository.existsById(notificationId)) {
            throw new NotificationNotFoundException(notificationId);
        }

        notificationRepository.deleteById(notificationId);
    }
}