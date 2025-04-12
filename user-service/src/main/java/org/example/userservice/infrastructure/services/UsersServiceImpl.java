package org.example.userservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.UserEditRequestModel;
import org.example.userservice.domain.services.UsersService;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {
    private final UserRepository userRepository;

    @Override
    public User getUserById(@NonNull UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    @Override
    public User editUserById(@NonNull UUID id, @Valid @NonNull UserEditRequestModel userEditModel) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        user.setName(userEditModel.getName());

        return userRepository.save(user);
    }

    @Override
    public User deleteUserById(@NonNull UUID id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        user.setActive(false);

        return userRepository.save(user);
    }
}
