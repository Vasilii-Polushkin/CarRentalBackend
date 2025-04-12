package org.example.userservice.domain.services;

import org.example.userservice.domain.models.entities.User;

import java.util.UUID;

public interface CurrentUserService {
    User getUser();

    String getEmail();

    UUID getId();
}
