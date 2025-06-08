package org.example.paymentservice.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateModelDto {
    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID carId;

    @NotNull
    private BigDecimal amount;
}