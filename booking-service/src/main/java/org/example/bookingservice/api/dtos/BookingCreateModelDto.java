package org.example.bookingservice.api.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateModelDto {
    @NotNull
    private UUID carId;

    @NotNull
    private UUID userId;

    @NotNull
    private LocalDateTime endDate;
}