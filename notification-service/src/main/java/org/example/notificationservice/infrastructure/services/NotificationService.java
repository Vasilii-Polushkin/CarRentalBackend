package org.example.notificationservice.infrastructure.services;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.api.dtos.NotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public void sendNotification(@NonNull UUID userId, @NonNull String message) {
        NotificationDto notification = NotificationDto.builder()
                .id(UUID.randomUUID())
                .message(message)
                .timestamp(Instant.now())
                .build();

        emailService.sendEmail(userId, "Car Rental Notification", message);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "user/queue/notifications",
                notification
        );
        log.info("Notification sent to user {}: {}", userId, message);
    }
}