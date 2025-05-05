package org.example.carservice.domain.models;

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
    private boolean isOnRepair;

    @NotNull
    private boolean isOnRental;

    @NotNull
    @NotBlank
    private String creatorName;

    @NotNull
    private UUID creatorId;
}
