package org.example.userservice.infrastructure.repositories;

import lombok.NonNull;
import org.example.userservice.domain.models.RefreshToken;
import org.example.userservice.domain.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByValue(@NonNull String refreshToken);
}