package com.hostel.management.dto.room;

import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequestDto {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Price per month is required")
    @Positive(message = "Price must be positive")
    private Double pricePerMonth;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String facilities;
    private String imageUrl;
    private RoomStatus status;
    private LocalDate vacancyDate;
}
