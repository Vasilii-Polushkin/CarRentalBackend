package org.example.paymentservice.api.mappers;

import org.example.common.dtos.PaymentCreateModelDto;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMapper {
    public PaymentRequestCreateModel toDomain(PaymentCreateModelDto dto) {
        return PaymentRequestCreateModel.builder()
                .bookingId(dto.getBookingId())
                .usdTotalAmount(dto.getUsdTotalAmount())
                .carId(dto.getCarId())
                .build();
    }
}
