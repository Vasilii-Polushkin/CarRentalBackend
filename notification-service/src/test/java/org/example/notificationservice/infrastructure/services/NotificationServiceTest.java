package org.example.notificationservice.infrastructure.services;

import org.example.notificationservice.api.dtos.NotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_MESSAGE = "Test notification message";

    @Test
    void sendNotification_shouldSendEmailAndWebSocketNotification() {
        notificationService.sendNotification(TEST_USER_ID, TEST_MESSAGE);

        verify(emailService).sendEmail(eq(TEST_USER_ID), eq("Car Rental Notification"), eq(TEST_MESSAGE));

        verify(messagingTemplate).convertAndSendToUser(
                eq(TEST_USER_ID.toString()),
                eq("user/queue/notifications"),
                argThat(notification -> {
                    NotificationDto n = (NotificationDto) notification;
                    return n.getMessage().equals(TEST_MESSAGE) &&
                            n.getId() != null &&
                            n.getTimestamp() != null;
                })
        );
    }

    @Test
    void sendNotification_shouldPropagateEmailServiceException() {
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendEmail(any(), any(), any());

        assertThrows(RuntimeException.class,
                () -> notificationService.sendNotification(TEST_USER_ID, TEST_MESSAGE));
    }

    @Test
    void sendNotification_shouldPropagateMessagingException() {
        doThrow(new RuntimeException("Messaging error"))
                .when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        assertThrows(RuntimeException.class,
                () -> notificationService.sendNotification(TEST_USER_ID, TEST_MESSAGE));
    }
}