package org.example.bookingservice.domain.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.common.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID carId;

    @NotNull
    private UUID userId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdTotalAmount;

    @NotNull
    private UUID paymentId;

    @NotNull
    private LocalDateTime createdAt;
}