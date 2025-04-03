package org.example.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.data.repositories.UserRepository;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.domain.models.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }
}