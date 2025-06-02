package org.example.common.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "payment-service",
        url = "http://localhost:8003/api/payment-service"
)
public interface PaymentServiceClient {
}