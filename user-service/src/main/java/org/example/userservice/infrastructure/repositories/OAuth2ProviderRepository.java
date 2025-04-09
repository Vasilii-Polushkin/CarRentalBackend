package org.example.userservice.infrastructure.repositories;

import lombok.NonNull;
import org.example.userservice.domain.models.entities.OAuth2Provider;
import org.example.userservice.domain.models.entities.RefreshToken;
import org.example.userservice.domain.models.entities.ids.OAuth2ProviderId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuth2ProviderRepository extends JpaRepository<OAuth2Provider, OAuth2ProviderId> {
}