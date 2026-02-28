package com.hostel.management.dto.resident;

import com.hostel.management.enums.ResidentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentResponseDto {

    private Long id;
    private String name;
    private String nic;
    private String contact;
    private String email;
    private String course;
    private Integer rating;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private ResidentStatus status;

    // Flattened room info
    private Long roomId;
    private String roomNumber;
    private String roomType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
