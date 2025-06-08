package org.example.paymentservice.api.mappers;

import org.example.paymentservice.api.dtos.PaymentCreateModelDto;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMapper {
    public PaymentRequestCreateModel toDomain(PaymentCreateModelDto dto) {
        return PaymentRequestCreateModel.builder()
                .bookingId(dto.getBookingId())
                .amount(dto.getAmount())
                .carId(dto.getCarId())
                .build();
    }
}
