package com.eilco.messagerie.services.interfaces;



import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;

import java.util.List;

public interface INotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    List<NotificationResponse> getNotificationsForUser(Long recipientId);

    List<NotificationResponse> getUnreadNotificationsForUser(Long recipientId);

    List<NotificationResponse> getNotificationsForGroup(Long groupId);

    NotificationResponse getNotificationById(Long notificationId);

    NotificationResponse markNotificationAsRead(Long notificationId);

    long markAllNotificationsAsReadForUser(Long recipientId);

    long getUnreadNotificationCount(Long recipientId);

    void deleteNotification(Long notificationId);

}
