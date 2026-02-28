package com.hostel.management.dto.room;

import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDto {

    private Long id;
    private String roomNumber;
    private RoomType roomType;
    private Double pricePerMonth;
    private Integer capacity;
    private Integer currentOccupancy;
    private Integer availableSpots;
    private List<String> facilities;
    private String imageUrl;
    private RoomStatus status;
    private LocalDate vacancyDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
