package com.hostel.management.dto.cleaning;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCleaningScheduleRequestDto {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Scheduled date is required")
    private String scheduledDate;

    private String assignedResidents;

    private String notes;
}
