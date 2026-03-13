package com.hostel.management.dto.visit;

import com.hostel.management.enums.VisitStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitResponseDto {

    private Long id;
    private String visitorName;
    private String visitorContact;
    private String visitorEmail;
    private String preferredRoomType;
    private String message;
    private LocalDate visitDate;
    private String visitTime;
    private VisitStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
