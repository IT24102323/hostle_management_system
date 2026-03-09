package com.hostel.management.dto.cleaning;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleaningTaskRequestDto {

    @NotBlank(message = "Area is required")
    private String area;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    @NotBlank(message = "Assigned staff is required")
    private String assignedStaff;

    private String assignedTo;
    private String scheduledDate;
    private String notes;
    private String completionStatus;
}
