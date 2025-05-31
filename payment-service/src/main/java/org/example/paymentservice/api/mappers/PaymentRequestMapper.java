package org.example.paymentservice.api.mappers;

import org.example.paymentservice.api.dtos.PaymentCreateModelDto;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMapper {
    public PaymentRequestCreateModel toDomain(PaymentCreateModelDto dto) {
        return PaymentRequestCreateModel.builder()
                .bookingId(dto.getBookingId())
                .payerId(dto.getPayerId())
                .amount(dto.getAmount())
                .build();
    }
}
