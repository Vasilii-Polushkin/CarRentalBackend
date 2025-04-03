package org.example.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RefreshTokenResponse {
    private String refreshToken;
}
