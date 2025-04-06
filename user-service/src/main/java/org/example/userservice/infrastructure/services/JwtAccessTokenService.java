package org.example.userservice.infrastructure.services;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.infrastructure.security.models.CarRentalUserDetails;
import org.example.userservice.api.mappers.RolesMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Service
public class JwtAccessTokenService {
    private static final String CLAIM_KEY_ROLES = "roles";

    private final JwtTokenService jwtUtil;
    private final SecretKey jwtAccessSecret;
    private final long jwtExpirationInMs;
    private final RolesMapper rolesMapper;

    public JwtAccessTokenService(
            JwtTokenService jwtUtil,
            @Value("${app.jwt.access.secret}") String jwtAccessSecret,
            @Value("${app.jwt.access.expiration-in-ms}") long jwtExpirationInMs,
            RolesMapper rolesMapper
    ) {
        this.jwtUtil = jwtUtil;
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.rolesMapper = rolesMapper;
    }

    //todo был лист
    public Set<Role> extractRoles(String token) {
        return rolesMapper.toRolesFromStrings(
                jwtUtil.<List<String>>extractClaim(token, jwtAccessSecret, CLAIM_KEY_ROLES)
        );
    }

    public String generateToken(CarRentalUserDetails userDetails) {
        var claims = new HashMap<String, Object>();

        List<String> roles = rolesMapper.toStrings(userDetails.getAuthorities());
        claims.put(CLAIM_KEY_ROLES, roles);

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
        return jwtUtil.isTokenValid(authToken, jwtAccessSecret);
    }
}