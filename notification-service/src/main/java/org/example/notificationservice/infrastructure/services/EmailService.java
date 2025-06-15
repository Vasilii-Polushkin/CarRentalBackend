package org.example.notificationservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.clients.UserServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserServiceClient userServiceClient;
    @Value("${app.user-service.api.key}")
    private String apiKey;

    @Async
    public void sendEmail(UUID userId, String subject, String content) {
        String email = userServiceClient.getUserByIdInternal(userId, apiKey).getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
        log.info("Email sent to {}: {}", email, subject);
    }
}