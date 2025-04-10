package org.example.userservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.domain.models.entities.RefreshToken;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.LoginRequestModel;
import org.example.userservice.domain.models.requests.RegisterRequestModel;
import org.example.userservice.infrastructure.repositories.RefreshTokenRepository;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.infrastructure.exceptions.AuthException;
import org.example.userservice.infrastructure.security.user_details.CarRentalUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Set;

//todo JwtModelDto to domain model
@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtAccessTokenService accessTokenService;
    private final JwtRefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CurrentUserService currentUserService;

    public JwtModelDto login(@Valid @NonNull LoginRequestModel authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User with email " + authRequest.getEmail() + " not found"));

        if (!Objects.equals(user.getPassword(), authRequest.getPassword())) {
            throw new AuthException("Wrong password");
        }

        return createAndSaveJwtToken(user);
    }

    public void revoke(String refreshTokenValue) {
        final RefreshToken savedRefreshToken = refreshTokenRepository
                .findByValue(refreshTokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found with value: " + refreshTokenValue));

        refreshTokenRepository.delete(savedRefreshToken);
    }

    public JwtModelDto register(@Valid @NonNull RegisterRequestModel authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())){
            throw new AuthException("Email already in use");
        }

        User user = new User();

        user.setEmail(authRequest.getEmail());
        user.setName(authRequest.getName());
        user.setPassword(authRequest.getPassword());
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);

        return createAndSaveJwtToken(user);
    }

    public JwtModelDto refreshAndRotate(@Valid @NonNull String refreshTokenValue) {
        final RefreshToken savedRefreshToken = refreshTokenRepository
                .findByValue(refreshTokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found with value: " + refreshTokenValue));

        final User user = savedRefreshToken.getUser();

        String newAccessTokenValue = accessTokenService.generateToken(new CarRentalUserDetails(user));
        String newRefreshTokenValue = refreshTokenService.generateToken(new CarRentalUserDetails(user));

        savedRefreshToken.setValue(newRefreshTokenValue);
        refreshTokenRepository.save(savedRefreshToken);

        return new JwtModelDto(newAccessTokenValue, newRefreshTokenValue);
    }

    public JwtModelDto createAndSaveJwtToken(User user) {
        String accessToken = accessTokenService.generateToken(new CarRentalUserDetails(user));
        String refreshToken = refreshTokenService.generateToken(new CarRentalUserDetails(user));

        RefreshToken refreshTokenModel = new RefreshToken();

        refreshTokenModel.setValue(refreshToken);
        refreshTokenModel.setUser(user);

        refreshTokenRepository.save(refreshTokenModel);

        return new JwtModelDto(accessToken, refreshToken);
    }
}