package org.example.bookingservice.api.mappers;

import org.example.bookingservice.api.dtos.BookingDto;
import org.example.bookingservice.domain.models.entities.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .carId(booking.getCarId())
                .userId(booking.getUserId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .totalPrice(booking.getUsdTotalAmount())
                .paymentId(booking.getPaymentId())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}