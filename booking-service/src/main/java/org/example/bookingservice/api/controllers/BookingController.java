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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final BookingCreateModelMapper bookingCreateModelMapper;

    @PostMapping
    public BookingDto createBooking(@RequestBody @Valid BookingCreateModelDto request) {
        return bookingMapper.toDto(
                bookingService.createBooking(bookingCreateModelMapper.toDomain(request))
        );
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') OR @bookingAccessManager.isOwner(#id)")
    public BookingDto cancelPayment(@PathVariable("id") UUID id) {
        return bookingMapper.toDto(
                bookingService.cancelBooking(id)
        );
    }

    @GetMapping("bookings/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @bookingAccessManager.isOwner(#id)")
    public BookingDto getBooking(@PathVariable @NotNull UUID id) {
        return bookingMapper.toDto(bookingService.getBookingById(id));
    }

    @GetMapping("/car/{id}/bookings")
    @RolesAllowed("ADMIN")
    public List<BookingDto> getAllBookingsByCarId(@PathVariable @NotNull UUID id) {
        return bookingService.getAllBookingsByCarId(id)
                .stream().map(bookingMapper::toDto).toList();
    }

    @GetMapping("/user/{id}/bookings")
    @PreAuthorize("hasRole('ADMIN') OR @currentUserService.userId==#id")
    public List<BookingDto> getAllBookingsByCreatorId(@PathVariable @NotNull UUID id) {
        return bookingService.getAllBookingsByUserId(id)
                .stream().map(bookingMapper::toDto).toList();
    }

    @GetMapping("bookings/my")
    public List<BookingDto> getAllCurrentUsersBookings() {
        return bookingService.getAllCurrentUsersBookings()
                .stream().map(bookingMapper::toDto).toList();
    }
}