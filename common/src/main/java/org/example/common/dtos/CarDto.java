package org.example.common.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.enums.CarStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDto {
    @NotNull
    public UUID id;

    @NotNull
    @NotBlank
    private String model;

    @NotNull
    private CarStatus status;

    @NotNull
    @NotBlank
    private String creatorName;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdPerHour;

    @NotNull
    private UUID creatorId;
}
