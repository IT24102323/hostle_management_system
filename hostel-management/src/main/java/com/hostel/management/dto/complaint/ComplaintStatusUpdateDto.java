package com.hostel.management.dto.complaint;

import com.hostel.management.enums.ComplaintStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintStatusUpdateDto {
    private ComplaintStatus status;
    private String resolution;
    private String adminNote;
}
