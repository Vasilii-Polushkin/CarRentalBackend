package org.example.userservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.UserEditModel;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;

    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public void editUserById(UUID id, UserEditModel userEditModel) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        user.setName(userEditModel.getName());

        userRepository.save(user);
    }
}
