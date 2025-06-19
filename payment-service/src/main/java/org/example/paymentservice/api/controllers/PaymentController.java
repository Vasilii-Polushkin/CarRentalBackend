package org.example.paymentservice.api.controllers;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.common.enums.PaymentStatus;
import org.example.common.dtos.PaymentDto;
import org.example.common.dtos.PaymentCreateModelDto;
import org.example.paymentservice.api.dtos.PaymentsPagedModelDto;
import org.example.paymentservice.api.mappers.PaymentMapper;
import org.example.paymentservice.api.mappers.PaymentRequestMapper;
import org.example.paymentservice.api.mappers.PaymentsPagedModelMapper;
import org.example.paymentservice.infrastructure.services.PaymentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final PaymentRequestMapper paymentRequestMapper;
    private final PaymentsPagedModelMapper paymentsPagedModelMapper;

    @PostMapping
    public PaymentDto createPayment(@RequestBody @Valid PaymentCreateModelDto paymentRequestDto) {
        return paymentMapper.toDto(
                paymentService.createPayment(paymentRequestMapper.toDomain(paymentRequestDto))
        );
    }

    @PutMapping("payments/{id}/perform")
    public PaymentDto performPayment(@PathVariable("id") UUID id) {
        return paymentMapper.toDto(
                paymentService.performPayment(id)
        );
    }

    @GetMapping("payments/pending")
    public List<PaymentDto> getUserPendingPaymentsByUserId(@NonNull UUID id) {
        return paymentService.findUserPaymentsByStatusAndCreatorId(PaymentStatus.PENDING, id)
                .stream().map(paymentMapper::toDto).toList();
    }

    @GetMapping("user/{id}/payments")
    @PreAuthorize("hasRole('ADMIN') OR @paymentAccessManager.isOwner(#id)")
    public PaymentsPagedModelDto getPaymentsPagedByUserId(
            @PathVariable("id") @Param("id") UUID id,
            @RequestParam(required = false, defaultValue = "0", name = "pageIndex") int pageIndex,
            @RequestParam(required = false, defaultValue = "30", name = "pageSize") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return paymentsPagedModelMapper.toDto(
                paymentService.getPaymentsPagedByUserId(id, pageable)
        );
    }

    @GetMapping("payments")
    @PreAuthorize("hasRole('ADMIN') OR @paymentAccessManager.isOwner(#id)")
    public PaymentsPagedModelDto getAllPaymentsPaged(
            @RequestParam(required = false, defaultValue = "0", name = "pageIndex") int pageIndex,
            @RequestParam(required = false, defaultValue = "30", name = "pageSize") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return paymentsPagedModelMapper.toDto(
                paymentService.getAllPaymentsPaged(pageable)
        );
    }

    @GetMapping("payments/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @paymentAccessManager.isOwner(#id)")
    public PaymentDto getPaymentById(@PathVariable("id") @Param("id") UUID id) {
        return paymentMapper.toDto(paymentService.findPaymentById(id));
    }
}