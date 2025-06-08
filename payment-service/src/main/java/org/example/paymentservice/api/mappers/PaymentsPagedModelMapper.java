package org.example.paymentservice.api.mappers;

import lombok.AllArgsConstructor;
import org.example.paymentservice.api.dtos.PaymentsPagedModelDto;
import org.example.paymentservice.domain.models.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentsPagedModelMapper {
    private final PaymentMapper paymentMapper;
    private final PaginationMapper paginationMapper;

    public PaymentsPagedModelDto toDto(Page<Payment> domainPage) {
        return new PaymentsPagedModelDto(
                domainPage
                        .getContent()
                        .stream()
                        .map(paymentMapper::toDto)
                        .toList(),
                paginationMapper.toDto(domainPage)
        );
    }
}