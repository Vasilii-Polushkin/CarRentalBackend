package org.example.carservice.domain.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.enums.CarStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @NotBlank
    private String model;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdPerHour;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CarStatus status;

    @NotNull
    private LocalDate creationDate;

    private LocalDate modificationDate;

    @NotNull
    @NotBlank
    private String creatorName;

    @NotNull
    private UUID creatorId;

    private LocalDateTime lockedUntil;
}
