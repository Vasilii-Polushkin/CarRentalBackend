package org.example.notificationservice.api.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.infrastructure.services.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("test/{id}")
    public void test(@PathVariable("id") @Parameter String id) {
        messagingTemplate.convertAndSendToUser(
                id,
                "user/queue/notifications",
                "notification"
        );
    }
}