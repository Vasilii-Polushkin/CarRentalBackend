package org.example.userservice.infrastructure.services;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.infrastructure.security.user_details.CarRentalUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
public class JwtRefreshTokenService {

    private final JwtTokenService jwtUtil;
    private final SecretKey jwtRefreshSecret;
    private final long jwtExpirationInMs;

    public JwtRefreshTokenService(
            JwtTokenService jwtUtil,
            @Value("${app.jwt.refresh.secret}") String jwtRefreshSecret,
            @Value("${app.jwt.refresh.expiration-in-ms}")long jwtExpirationInMs
    ) {
        this.jwtUtil = jwtUtil;
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(CarRentalUserDetails userDetails) {
        return jwtUtil.generateToken(userDetails, jwtExpirationInMs, jwtRefreshSecret, new HashMap<>());
    }

    private Boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token, jwtRefreshSecret);
    }

    public String extractEmail(String token) {
        return jwtUtil.extractEmail(token, jwtRefreshSecret);
    }

    public Date extractExpiration(String token) {
        return jwtUtil.extractExpiration(token, jwtRefreshSecret);
    }

    public boolean validateToken(String authToken) {
        return jwtUtil.isTokenValid(authToken, jwtRefreshSecret);
    }
}