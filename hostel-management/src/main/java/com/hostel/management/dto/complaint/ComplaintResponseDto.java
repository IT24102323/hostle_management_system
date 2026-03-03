package com.hostel.management.dto.complaint;

import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponseDto {

    private Long id;
    private Long residentId;
    private String residentName;
    private String roomNumber;
    private String title;
    private String description;
    private String category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private String resolution;
    private String adminNote;
    private Integer reminderCount;
    private LocalDateTime lastReminderAt;
    private boolean reminderAllowed;   // student can send reminder
    private boolean editAllowed;        // student can edit
    private boolean deleteAllowed;      // student can delete
    private long unreadNotifications;
    private LocalDate complaintDate;
    private LocalDate resolvedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
