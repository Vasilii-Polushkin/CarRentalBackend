package org.example.bookingservice.api.controllers;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.bookingservice.api.dtos.BookingCreateModelDto;
import org.example.bookingservice.api.dtos.BookingDto;
import org.example.bookingservice.api.mappers.BookingCreateModelMapper;
import org.example.bookingservice.api.mappers.BookingMapper;
import org.example.bookingservice.infrastructure.services.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final BookingCreateModelMapper bookingCreateModelMapper;

    @PostMapping
    public BookingDto createBooking(@RequestBody @Valid BookingCreateModelDto request) {
        // todo mb check for request's user id and user id mismatch
        return bookingMapper.toDto(
                bookingService.createBooking(bookingCreateModelMapper.toDomain(request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @accessChecker.isSelf(#userId)")//todo impl + make common util
    public BookingDto getBooking(@PathVariable @NotNull UUID id) {
        return bookingMapper.toDto(bookingService.getBookingById(id));
    }
}