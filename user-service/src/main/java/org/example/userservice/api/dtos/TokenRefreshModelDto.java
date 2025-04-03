package org.example.userservice.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshModelDto {
    @NotBlank
    @NotNull
    private String refreshToken;
}