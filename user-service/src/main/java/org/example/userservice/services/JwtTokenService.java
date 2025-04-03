package org.example.userservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.domain.models.CarRentalUserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtTokenService {
    public String generateToken(
            CarRentalUserDetails userDetails,
            long jwtExpirationInMs,
            SecretKey secretKey,
            Map<String, Object> claims
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public Boolean isTokenExpired(String token, SecretKey secretKey) {
        return extractExpiration(token, secretKey).before(new Date());
    }

    public String extractEmail(String token, SecretKey secretKey) {
        return extractClaim(token, Claims::getSubject, secretKey);
    }

    public Date extractExpiration(String token, SecretKey secretKey) {
        return extractClaim(token, Claims::getExpiration, secretKey);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, SecretKey secretKey) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken, SecretKey secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error(ex.getMessage());
        }
        return false;
    }
}
