package com.hostel.management.dto.complaint;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintNotificationDto {
    private Long id;
    private Long complaintId;
    private String complaintTitle;
    private Long residentId;
    private String type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
