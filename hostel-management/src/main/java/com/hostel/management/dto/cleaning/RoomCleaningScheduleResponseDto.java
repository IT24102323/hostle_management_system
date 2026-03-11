package com.hostel.management.dto.cleaning;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCleaningScheduleResponseDto {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private LocalDate scheduledDate;
    private Integer weekNumber;
    private Integer year;
    private String status;
    private String assignedResidents;
    private String notes;
    private LocalDateTime completedAt;
    private String completedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
