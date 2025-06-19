package org.example.common.feign.clients;

import org.example.common.dtos.CarCreateModelDto;
import org.example.common.dtos.CarDto;
import org.example.common.dtos.PaymentCreateModelDto;
import org.example.common.dtos.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        url = "http://localhost:8003/api/payment-service"
)
public interface PaymentServiceClient {
    @PostMapping("payments")
    PaymentDto createPayment(@RequestBody PaymentCreateModelDto createModel);
}