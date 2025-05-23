package org.example.carservice.api.dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDto {
    @NotNull
    private UUID id;

    @NotNull
    @NotBlank
    private String model;

    @NotNull
    private boolean isOnRepair;

    @NotNull
    private boolean isOnRental;

    @NotNull
    private LocalDate creationDate;

    private LocalDate modificationDate;

    @NotNull
    @NotBlank
    private String creatorName;

    @NotNull
    private UUID creatorId;
}
