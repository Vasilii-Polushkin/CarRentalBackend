package org.example.paymentservice.api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.enums.PaymentStatus;
import org.example.paymentservice.api.dtos.PaymentDto;
import org.example.paymentservice.api.dtos.PaymentCreateModelDto;
import org.example.paymentservice.api.mappers.PaymentMapper;
import org.example.paymentservice.api.mappers.PaymentRequestMapper;
import org.example.paymentservice.infrastructure.services.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final PaymentRequestMapper paymentRequestMapper;

    @PostMapping
    public PaymentDto createPayment(@RequestBody @Valid PaymentCreateModelDto paymentRequestDto) {
        return paymentMapper.toDto(
                paymentService.createPayment(paymentRequestMapper.toDomain(paymentRequestDto))
        );
    }

    @PutMapping("/{id}/cancel")
    public PaymentDto cancelPayment(@PathVariable("id") UUID id) {
        return paymentMapper.toDto(
                paymentService.cancelPayment(id)
        );
    }

    @PutMapping("/{id}/perform")
    public PaymentDto performPayment(@PathVariable("id") UUID id) {
        return paymentMapper.toDto(
                paymentService.performPayment(id)
        );
    }

    @GetMapping("/pending")
    public List<PaymentDto> getPendingPayments() {
        return paymentService.findCurrentUsersPaymentsByStatus(PaymentStatus.PENDING)
                .stream().map(paymentMapper::toDto).toList();
    }
}