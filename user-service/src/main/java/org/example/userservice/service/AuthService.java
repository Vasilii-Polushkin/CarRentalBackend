package org.example.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.data.repositories.RefreshTokenRepository;
import org.example.userservice.domain.models.*;
import org.example.userservice.exceptions.AuthException;
import org.example.userservice.services.JwtAccessTokenService;
import org.example.userservice.services.JwtRefreshTokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtAccessTokenService accessTokenService;
    private final JwtRefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtModelDto login(@NonNull LoginRequestModel authRequest) {
        User user = userService.findByEmail(authRequest.getEmail());

        if (!Objects.equals(user.getPassword(), authRequest.getPassword())) {
            throw new AuthException("Неправильный пароль");
        }

        String accessToken = accessTokenService.generateToken(new CarRentalUserDetails(user));
        String refreshToken = refreshTokenService.generateToken(new CarRentalUserDetails(user));

        RefreshToken refreshTokenModel = new RefreshToken();
        refreshTokenModel.setValue(refreshToken);
        refreshTokenModel.setUser(user);

        refreshTokenRepository.save(refreshTokenModel);

        return new JwtModelDto(accessToken, refreshToken);
    }

    public JwtModelDto refreshAndRotate(@NonNull String refreshTokenValue) {
        final RefreshToken savedRefreshToken = refreshTokenRepository
                .findByValue(refreshTokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found with value: " + refreshTokenValue));

        final User user = savedRefreshToken.getUser();

        String newAccessToken = accessTokenService.generateToken(new CarRentalUserDetails(user));
        String newRefreshToken = refreshTokenService.generateToken(new CarRentalUserDetails(user));

        return new JwtModelDto(newAccessToken, newRefreshToken);
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}