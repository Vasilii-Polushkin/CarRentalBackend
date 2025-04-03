package org.example.userservice.services;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.domain.models.CarRentalUserDetails;
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

    public JwtRefreshTokenService(
            JwtTokenService jwtUtil,
            @Value("${app.jwt.refresh.secret}") String jwtRefreshSecret
    ) {
        this.jwtUtil = jwtUtil;
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    @Value("${app.jwt.refresh.expiration-in-ms}")
    private long jwtExpirationInMs;

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
        return jwtUtil.validateToken(authToken, jwtRefreshSecret);
    }
}