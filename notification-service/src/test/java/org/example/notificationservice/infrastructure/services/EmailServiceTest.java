package org.example.notificationservice.infrastructure.services;

import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;
import org.example.common.dtos.UserDto;
import org.example.common.feign.clients.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private EmailService emailService;

    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_EMAIL = "user@example.com";
    private final String TEST_SUBJECT = "Test Subject";
    private final String TEST_CONTENT = "Test email content";

    @Test
    void sendEmail_shouldSendEmailWithCorrectParameters() {
        UserDto userDto = new UserDto();
        userDto.setEmail(TEST_EMAIL);
        when(userServiceClient.getUserByIdInternal(any(), any())).thenReturn(userDto);

        emailService.sendEmail(TEST_USER_ID, TEST_SUBJECT, TEST_CONTENT);

        verify(userServiceClient).getUserByIdInternal(any(), any());
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                Objects.requireNonNull(message.getTo())[0].equals(TEST_EMAIL) &&
                        Objects.equals(message.getSubject(), TEST_SUBJECT) &&
                        Objects.equals(message.getText(), TEST_CONTENT)
        ));
    }
}