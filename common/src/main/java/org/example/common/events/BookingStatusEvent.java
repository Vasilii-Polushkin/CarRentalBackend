package org.example.common.events;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusEvent {
    @NotNull
    private UUID bookingId;

    private UUID paymentId;

    @NotNull
    private UUID carId;

    @NotNull
    private UUID userId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdTotalAmount;

    @NotNull
    private BookingStatus status;

    @NotNull
    private LocalDateTime timestamp;
}