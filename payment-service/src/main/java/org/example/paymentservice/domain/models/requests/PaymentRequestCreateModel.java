package org.example.paymentservice.domain.models.requests;

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
public class PaymentRequestCreateModel {
    private UUID bookingId;
    private UUID payerId;
    private BigDecimal amount;
}