package org.example.carservice.domain.models.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CarEditRequestModel {
    @NotNull
    @NotBlank
    private String model;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdPerHour;
}