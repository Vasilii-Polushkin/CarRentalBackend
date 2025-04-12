package org.example.userservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.domain.models.entities.RefreshToken;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.LoginRequestModel;
import org.example.userservice.domain.models.requests.RegisterRequestModel;
import org.example.userservice.domain.models.responses.JwtModel;
import org.example.userservice.infrastructure.repositories.RefreshTokenRepository;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.infrastructure.exceptions.AuthException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtAccessTokenUtil accessTokenUtil;
    private final JwtRefreshTokenUtil refreshTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtModel login(@Valid @NonNull LoginRequestModel authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User with email " + authRequest.getEmail() + " not found"));

        if (!Objects.equals(user.getPassword(), authRequest.getPassword())) {
            throw new AuthException("Wrong password");
        }

        return createAndSaveJwtToken(user);
    }

    public void revoke(@NonNull String refreshTokenValue) {
        final RefreshToken savedRefreshToken = refreshTokenRepository
                .findByValue(refreshTokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found with value: " + refreshTokenValue));

        refreshTokenRepository.delete(savedRefreshToken);
    }

    public JwtModel register(@Valid @NonNull RegisterRequestModel authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())){
            throw new AuthException("Email already in use");
        }

        User user = User
                .builder()
                .email(authRequest.getEmail())
                .name(authRequest.getName())
                .password(authRequest.getPassword())
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();

        userRepository.save(user);

        return createAndSaveJwtToken(user);
    }

    public JwtModel refreshAndRotate(@Valid @NonNull String refreshTokenValue) {
        final RefreshToken savedRefreshToken = refreshTokenRepository
                .findByValue(refreshTokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found with value: " + refreshTokenValue));

        final User user = savedRefreshToken.getUser();

        String newAccessTokenValue = accessTokenUtil.generateToken(user);
        String newRefreshTokenValue = refreshTokenUtil.generateToken(user);

        savedRefreshToken.setValue(newRefreshTokenValue);
        refreshTokenRepository.save(savedRefreshToken);

        return new JwtModel(newAccessTokenValue, newRefreshTokenValue);
    }

    public JwtModel createAndSaveJwtToken(@Valid @NonNull User user) {
        String accessToken = accessTokenUtil.generateToken(user);
        String refreshToken = refreshTokenUtil.generateToken(user);

        RefreshToken refreshTokenModel = new RefreshToken();

        refreshTokenModel.setValue(refreshToken);
        refreshTokenModel.setUser(user);

        refreshTokenRepository.save(refreshTokenModel);

        return new JwtModel(accessToken, refreshToken);
    }
}