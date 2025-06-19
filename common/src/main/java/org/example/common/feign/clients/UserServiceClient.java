package org.example.common.feign.clients;

import org.example.common.dtos.UserDto;
import org.example.common.headers.CustomHeaders;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Component
@FeignClient(
        name = "user-service",
        url = "http://localhost:8000/api/user-service"
)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserDto getUserById(@PathVariable("id") UUID id);

    @GetMapping("/internal/users/{id}")
    UserDto getUserByIdInternal(@PathVariable("id") UUID id,
                                @RequestHeader(CustomHeaders.API_KEY_HEADER) String apiKey);

    @DeleteMapping("/users/{id}")
    void deleteUserById(@PathVariable("id") UUID id);
}