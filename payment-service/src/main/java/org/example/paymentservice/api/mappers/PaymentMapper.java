package org.example.paymentservice.api.mappers;

import org.example.common.dtos.PaymentDto;
import org.example.paymentservice.domain.models.entities.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentDto toDto (Payment model){
        return PaymentDto.builder()
                .id(model.getId())
                .cardId(model.getCarId())
                .status(model.getStatus())
                .creatorId(model.getCreatorId())
                .usdTotalAmount(model.getUsdTotalAmount())
                .bookingId(model.getBookingId())
                .build();
    }
}
