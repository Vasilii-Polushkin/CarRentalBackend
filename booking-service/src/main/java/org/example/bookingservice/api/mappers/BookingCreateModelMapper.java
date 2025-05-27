package org.example.bookingservice.api.mappers;

import org.example.bookingservice.api.dtos.BookingCreateModelDto;
import org.example.bookingservice.domain.models.requests.BookingCreateRequestModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingCreateModelMapper {
    public BookingCreateRequestModel toDomain(BookingCreateModelDto request) {
        return BookingCreateRequestModel.builder()
                .carId(request.getCarId())
                .userId(request.getUserId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }
}