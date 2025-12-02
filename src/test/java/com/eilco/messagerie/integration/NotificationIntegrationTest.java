package com.eilco.messagerie.integration;

import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.NotificationRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.Notification;
import com.eilco.messagerie.repositories.entities.NotificationType;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'integration pour NotificationService
 * Utilise H2 Database en memoire pour tester l'integration complete
 * avec la base de donnees, repositories, mappers et service layer
 */
class NotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User testSender;
    private User testRecipient;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de donnees H2 avant chaque test
        notificationRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();

        // Creer un utilisateur expediteur dans H2
        testSender = new User();
        testSender.setUsername("sender_user");
        testSender.setPassword("password123");
        testSender.setFirstName("Safa");
        testSender.setLastName("Sender");
        testSender = userRepository.save(testSender);

        // Creer un utilisateur destinataire dans H2
        testRecipient = new User();
        testRecipient.setUsername("recipient_user");
        testRecipient.setPassword("password456");
        testRecipient.setFirstName("Nawal");
        testRecipient.setLastName("Recipient");
        testRecipient = userRepository.save(testRecipient);

        // Creer un message de test dans H2
        testMessage = new Message();
        testMessage.setContent("Test message content");
        testMessage.setSender(testSender);
        testMessage.setReceiverUser(testRecipient);
        testMessage.setTimestamp(LocalDateTime.now());
        testMessage = messageRepository.save(testMessage);

        System.out.println("=== SETUP COMPLETE ===");
        System.out.println("Sender User ID: " + testSender.getId());
        System.out.println("Recipient User ID: " + testRecipient.getId());
        System.out.println("Message ID: " + testMessage.getId());
    }

    @Test
    @DisplayName("Test 1: Creer une notification avec succes")
    void testCreateNotification_Success() {
        // ARRANGE - Preparer les donnees de test
        NotificationRequest request = NotificationRequest.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .build();

        // ACT - Executer l'action a tester
        NotificationResponse response = notificationService.createNotification(request);

        // ASSERT - Verifier les resultats
        assertNotNull(response, "La response ne doit pas etre null");
        assertNotNull(response.getId(), "L'ID de la notification doit etre genere");
        assertEquals(testMessage.getId(), response.getMessageId(), "Le message ID doit correspondre");
        assertEquals(NotificationType.PRIVATE_MESSAGE, response.getType(), "Le type doit etre PRIVATE_MESSAGE");
        assertEquals(testSender.getId(), response.getSenderId(), "Le sender ID doit correspondre");
        assertEquals(testRecipient.getId(), response.getRecipientId(), "Le recipient ID doit correspondre");
        assertNotNull(response.getSentAt(), "La date d'envoi doit etre definie");
        assertFalse(response.getStatus(), "Le statut doit etre false (non lu) par defaut");

        // Verifier dans la base de donnees H2
        Notification savedNotification = notificationRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedNotification, "La notification doit etre sauvegardee dans H2");
        assertEquals(testRecipient.getId(), savedNotification.getRecipientId(), "Le recipient ID doit correspondre dans la DB");
    }

    @Test
    @DisplayName("Test 2: Recuperer toutes les notifications d'un utilisateur")
    void testGetNotificationsForUser_Success() {
        // ARRANGE - Creer plusieurs notifications dans H2
        Notification notif1 = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false)
                .build();
        notificationRepository.save(notif1);

        Notification notif2 = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(true)
                .build();
        notificationRepository.save(notif2);

        Notification notif3 = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_INVITE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false)
                .build();
        notificationRepository.save(notif3);

        // ACT - Recuperer toutes les notifications pour le recipient
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(testRecipient.getId());

        // ASSERT
        assertNotNull(notifications, "La liste ne doit pas etre null");
        assertEquals(3, notifications.size(), "Il doit y avoir 3 notifications");

        // Verifier que toutes les notifications appartiennent au recipient
        notifications.forEach(notif ->
                assertEquals(testRecipient.getId(), notif.getRecipientId(),
                        "Toutes les notifications doivent appartenir au recipient")
        );
    }

    @Test
    @DisplayName("Test 3: Recuperer uniquement les notifications non lues")
    void testGetUnreadNotificationsForUser_Success() {
        // ARRANGE - Creer des notifications lues et non lues
        Notification unreadNotif1 = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false) // NON LU
                .build();
        notificationRepository.save(unreadNotif1);

        Notification readNotif = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(true) // LU
                .build();
        notificationRepository.save(readNotif);

        Notification unreadNotif2 = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_INVITE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false) // NON LU
                .build();
        notificationRepository.save(unreadNotif2);

        // ACT - Recuperer uniquement les notifications non lues
        List<NotificationResponse> unreadNotifications = notificationService.getUnreadNotificationsForUser(testRecipient.getId());

        // ASSERT
        assertNotNull(unreadNotifications, "La liste ne doit pas etre null");
        assertEquals(2, unreadNotifications.size(), "Il doit y avoir 2 notifications non lues");

        // Verifier que toutes sont non lues
        unreadNotifications.forEach(notif ->
                assertFalse(notif.getStatus(), "Toutes les notifications doivent etre non lues (status = false)")
        );
    }

    @Test
    @DisplayName("Test 4: Marquer une notification comme lue")
    void testMarkNotificationAsRead_Success() {
        // ARRANGE - Creer une notification non lue
        Notification unreadNotification = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false) // NON LU
                .build();
        unreadNotification = notificationRepository.save(unreadNotification);

        Long notificationId = unreadNotification.getId();

        // ACT - Marquer la notification comme lue
        NotificationResponse response = notificationService.markNotificationAsRead(notificationId);

        // ASSERT
        assertNotNull(response, "La response ne doit pas etre null");
        assertTrue(response.getStatus(), "Le statut doit etre true (lu) apres le marquage");

        // Verifier dans la base de donnees H2
        Notification updatedNotification = notificationRepository.findById(notificationId).orElse(null);
        assertNotNull(updatedNotification, "La notification doit toujours exister dans H2");
        assertTrue(updatedNotification.getStatus(), "Le statut dans la DB doit etre true (lu)");
    }

    @Test
    @DisplayName("Test 5: Marquer toutes les notifications d'un utilisateur comme lues")
    void testMarkAllNotificationsAsReadForUser_Success() {
        // ARRANGE - Creer plusieurs notifications non lues
        for (int i = 0; i < 5; i++) {
            Notification notification = Notification.builder()
                    .messageId(testMessage.getId())
                    .type(NotificationType.PRIVATE_MESSAGE)
                    .senderId(testSender.getId())
                    .recipientId(testRecipient.getId())
                    .sentAt(LocalDateTime.now())
                    .status(false) // NON LU
                    .build();
            notificationRepository.save(notification);
        }

        // Verifier qu'il y a bien 5 notifications non lues
        long unreadCountBefore = notificationService.getUnreadNotificationCount(testRecipient.getId());
        assertEquals(5, unreadCountBefore, "Il doit y avoir 5 notifications non lues au depart");

        // ACT - Marquer toutes comme lues
        long markedCount = notificationService.markAllNotificationsAsReadForUser(testRecipient.getId());

        // ASSERT
        assertEquals(5, markedCount, "5 notifications doivent avoir ete marquees comme lues");

        // Verifier qu'il n'y a plus de notifications non lues
        long unreadCountAfter = notificationService.getUnreadNotificationCount(testRecipient.getId());
        assertEquals(0, unreadCountAfter, "Il ne doit plus y avoir de notifications non lues");

        // Verifier dans la base de donnees H2
        List<Notification> allNotifications = notificationRepository.findByRecipientId(testRecipient.getId());
        allNotifications.forEach(notif ->
                assertTrue(notif.getStatus(), "Toutes les notifications doivent etre marquees comme lues")
        );
    }

    @Test
    @DisplayName("Test 6: Compter les notifications non lues d'un utilisateur")
    void testGetUnreadNotificationCount_Success() {
        // ARRANGE - Creer 3 notifications non lues et 2 lues
        for (int i = 0; i < 3; i++) {
            Notification unreadNotif = Notification.builder()
                    .messageId(testMessage.getId())
                    .type(NotificationType.PRIVATE_MESSAGE)
                    .senderId(testSender.getId())
                    .recipientId(testRecipient.getId())
                    .sentAt(LocalDateTime.now())
                    .status(false) // NON LU
                    .build();
            notificationRepository.save(unreadNotif);
        }

        for (int i = 0; i < 2; i++) {
            Notification readNotif = Notification.builder()
                    .messageId(testMessage.getId())
                    .type(NotificationType.GROUP_MESSAGE)
                    .senderId(testSender.getId())
                    .recipientId(testRecipient.getId())
                    .sentAt(LocalDateTime.now())
                    .status(true) // LU
                    .build();
            notificationRepository.save(readNotif);
        }

        // ACT - Compter les notifications non lues
        long unreadCount = notificationService.getUnreadNotificationCount(testRecipient.getId());

        // ASSERT
        assertEquals(3, unreadCount, "Il doit y avoir exactement 3 notifications non lues");
    }

    @Test
    @DisplayName("Test 7: Recuperer une notification specifique par ID")
    void testGetNotificationById_Success() {
        // ARRANGE - Creer une notification dans H2
        Notification notification = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_INVITE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .groupId(100L)
                .sentAt(LocalDateTime.now())
                .status(false)
                .build();
        notification = notificationRepository.save(notification);

        Long notificationId = notification.getId();

        // ACT - Recuperer la notification par ID
        NotificationResponse response = notificationService.getNotificationById(notificationId);

        // ASSERT
        assertNotNull(response, "La response ne doit pas etre null");
        assertEquals(notificationId, response.getId(), "L'ID doit correspondre");
        assertEquals(NotificationType.GROUP_INVITE, response.getType(), "Le type doit correspondre");
        assertEquals(100L, response.getGroupId(), "Le group ID doit correspondre");
        assertEquals(testSender.getId(), response.getSenderId(), "Le sender ID doit correspondre");
        assertEquals(testRecipient.getId(), response.getRecipientId(), "Le recipient ID doit correspondre");
    }

    @Test
    @DisplayName("Test 8: Supprimer une notification")
    void testDeleteNotification_Success() {
        // ARRANGE - Creer une notification dans H2
        Notification notification = Notification.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .sentAt(LocalDateTime.now())
                .status(false)
                .build();
        notification = notificationRepository.save(notification);

        Long notificationId = notification.getId();

        // Verifier que la notification existe
        assertTrue(notificationRepository.existsById(notificationId),
                "La notification doit exister avant suppression");

        // ACT - Supprimer la notification
        notificationService.deleteNotification(notificationId);

        // ASSERT
        assertFalse(notificationRepository.existsById(notificationId),
                "La notification ne doit plus exister apres suppression");
    }

    @Test
    @DisplayName("Test 9: Creer une notification de type GROUP_MESSAGE avec groupId")
    void testCreateGroupNotification_Success() {
        // ARRANGE
        Long testGroupId = 999L;
        NotificationRequest request = NotificationRequest.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.GROUP_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(testRecipient.getId())
                .groupId(testGroupId)
                .build();

        // ACT
        NotificationResponse response = notificationService.createNotification(request);

        // ASSERT
        assertNotNull(response, "La response ne doit pas etre null");
        assertEquals(NotificationType.GROUP_MESSAGE, response.getType(), "Le type doit etre GROUP_MESSAGE");
        assertEquals(testGroupId, response.getGroupId(), "Le group ID doit correspondre");

        // Verifier dans H2
        Notification savedNotification = notificationRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedNotification, "La notification doit etre sauvegardee");
        assertEquals(testGroupId, savedNotification.getGroupId(), "Le group ID doit etre sauvegarde");
    }

    @Test
    @DisplayName("Test 10: Gestion d'erreur - Recuperer une notification inexistante")
    void testGetNotificationById_NotFound() {
        // ARRANGE
        Long nonExistentId = 99999L;

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.getNotificationById(nonExistentId);
        }, "Une exception doit etre levee pour une notification inexistante");

        assertTrue(exception.getMessage().contains("not found"),
                "Le message d'erreur doit indiquer que la notification n'existe pas");
    }

    @Test
    @DisplayName("Test 11: Gestion d'erreur - Supprimer une notification inexistante")
    void testDeleteNotification_NotFound() {
        // ARRANGE
        Long nonExistentId = 99999L;

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.deleteNotification(nonExistentId);
        }, "Une exception doit etre levee lors de la suppression d'une notification inexistante");

        assertTrue(exception.getMessage().contains("not found"),
                "Le message d'erreur doit indiquer que la notification n'existe pas");
    }

    @Test
    @DisplayName("Test 12: Gestion d'erreur - Creer une notification sans recipientId")
    void testCreateNotification_MissingRecipientId() {
        // ARRANGE
        NotificationRequest request = NotificationRequest.builder()
                .messageId(testMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(testSender.getId())
                .recipientId(null) // MANQUANT
                .build();

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(request);
        }, "Une exception doit etre levee si recipientId est null");

        assertTrue(exception.getMessage().contains("required"),
                "Le message d'erreur doit indiquer que recipientId est requis");
    }

    @Test
    @DisplayName("Test 13: Recuperer les notifications d'un groupe")
    void testGetNotificationsForGroup_Success() {
        // ARRANGE
        Long testGroupId = 777L;

        // Creer des notifications pour le groupe
        for (int i = 0; i < 4; i++) {
            Notification notification = Notification.builder()
                    .messageId(testMessage.getId())
                    .type(NotificationType.GROUP_MESSAGE)
                    .senderId(testSender.getId())
                    .recipientId(testRecipient.getId())
                    .groupId(testGroupId)
                    .sentAt(LocalDateTime.now())
                    .status(false)
                    .build();
            notificationRepository.save(notification);
        }

        // ACT
        List<NotificationResponse> groupNotifications = notificationService.getNotificationsForGroup(testGroupId);

        // ASSERT
        assertNotNull(groupNotifications, "La liste ne doit pas etre null");
        assertEquals(4, groupNotifications.size(), "Il doit y avoir 4 notifications pour le groupe");

        // Verifier que toutes appartiennent au groupe
        groupNotifications.forEach(notif ->
                assertEquals(testGroupId, notif.getGroupId(),
                        "Toutes les notifications doivent appartenir au groupe specifie")
        );
    }

    @Test
    @DisplayName("Test 14: Verifier l'isolation des tests - Base vide au debut")
    void testDatabaseIsolation_EmptyAtStart() {
        // ASSERT - La base ne contient que les donnees du setUp
        List<Notification> allNotifications = notificationRepository.findAll();
        assertEquals(0, allNotifications.size(),
                "La base doit etre vide de notifications au debut du test (isolation)");
    }
}
