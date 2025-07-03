package org.example.bookingservice.api.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.bookingservice.api.dtos.BookingCreateModelDto;
import org.example.bookingservice.api.dtos.BookingDto;
import org.example.bookingservice.api.mappers.BookingCreateModelMapper;
import org.example.bookingservice.api.mappers.BookingMapper;
import org.example.bookingservice.infrastructure.services.BookingService;
import org.example.common.enums.Role;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final BookingCreateModelMapper bookingCreateModelMapper;

    @PostMapping("bookings")
    public BookingDto createBooking(@RequestBody @Valid BookingCreateModelDto request) {
        return bookingMapper.toDto(
                bookingService.createBooking(bookingCreateModelMapper.toDomain(request))
        );
    }

    @PutMapping("bookings/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') OR @bookingAccessManager.isOwner(#id)")
    public BookingDto cancelBooking(@PathVariable("id") @Param("id") UUID id) {
        return bookingMapper.toDto(
                bookingService.cancelBooking(id)
        );
    }

    @GetMapping("bookings/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @bookingAccessManager.isOwner(#id)")
    public BookingDto getBooking(@PathVariable("id") @Param("id") @NotNull UUID id) {
        return bookingMapper.toDto(bookingService.getBookingById(id));
    }

    @GetMapping("/car/{id}/bookings")
    @RolesAllowed("ADMIN")
    public List<BookingDto> getAllBookingsByCarId(@PathVariable("id") @NotNull UUID id) {
        return bookingService.getAllBookingsByCarId(id)
                .stream().map(bookingMapper::toDto).toList();
    }

    @GetMapping("/user/{id}/bookings")
    @PreAuthorize("hasRole('ADMIN') OR @currentUserService.userId==#id")
    public List<BookingDto> getAllBookingsByCreatorId(@PathVariable("id") @Param("id") @NotNull UUID id) {
        return bookingService.getAllBookingsByUserId(id)
                .stream().map(bookingMapper::toDto).toList();
    }
}