package org.example.bookingservice.api.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.common.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
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
    private BookingStatus status;

    private Double totalPrice;

    private UUID paymentId;

    @NotNull
    private LocalDateTime createdAt;
}