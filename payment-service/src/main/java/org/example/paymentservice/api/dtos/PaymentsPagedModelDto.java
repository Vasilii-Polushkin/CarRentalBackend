package org.example.paymentservice.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.dtos.PaginationDto;
import org.example.common.dtos.PaymentDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentsPagedModelDto {
    List<PaymentDto> payments;

    @NotNull
    PaginationDto pagination;
}