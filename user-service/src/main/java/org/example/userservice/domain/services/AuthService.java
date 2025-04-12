package org.example.userservice.domain.services;

import jakarta.validation.Valid;
import lombok.NonNull;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.LoginRequestModel;
import org.example.userservice.domain.models.requests.RegisterRequestModel;
import org.example.userservice.domain.models.responses.JwtModel;

public interface AuthService {
    JwtModel login(@Valid @NonNull LoginRequestModel authRequest);

    void revoke(@NonNull String refreshTokenValue);

    JwtModel register(@Valid @NonNull RegisterRequestModel authRequest);

    JwtModel refreshAndRotate(@Valid @NonNull String refreshTokenValue);

    JwtModel createAndSaveJwtToken(@Valid @NonNull User user);
}
