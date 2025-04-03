package org.example.userservice.services;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.domain.models.CarRentalUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
public class JwtAccessTokenService {

    private final JwtTokenService jwtUtil;
    private final SecretKey jwtAccessSecret;

    public JwtAccessTokenService(
            JwtTokenService jwtUtil,
            @Value("${app.jwt.access.secret}") String jwtAccessSecret
    ) {
        this.jwtUtil = jwtUtil;
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
    }

    @Value("${app.jwt.access.expiration-in-ms}")
    private long jwtExpirationInMs;

    public String extractRoles(String token) {
        return jwtUtil.extractEmail(token, jwtAccessSecret);
    }

    public String generateToken(CarRentalUserDetails userDetails) {
        var claims = new HashMap<String, Object>();

        return jwtUtil.generateToken(userDetails, jwtExpirationInMs, jwtAccessSecret, claims);
    }

    private Boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token, jwtAccessSecret);
    }

    public String extractEmail(String token) {
        return jwtUtil.extractEmail(token, jwtAccessSecret);
    }

    public Date extractExpiration(String token) {
        return jwtUtil.extractExpiration(token, jwtAccessSecret);
    }

    public boolean validateToken(String authToken) {
        return jwtUtil.validateToken(authToken, jwtAccessSecret);
    }
}