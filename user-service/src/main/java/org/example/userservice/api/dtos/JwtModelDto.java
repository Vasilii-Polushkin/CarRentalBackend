package org.example.userservice.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtModelDto {
    @NotNull
    @NotBlank
    String accessToken;

    @NotNull
    @NotBlank
    private String refreshToken;
}
