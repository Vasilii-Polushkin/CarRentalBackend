package org.example.carservice.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CarEditModelDto {
    @NotNull
    @NotBlank
    private String model;

    @NotNull
    private boolean isOnRepair;

    @NotNull
    private boolean isOnRental;
}