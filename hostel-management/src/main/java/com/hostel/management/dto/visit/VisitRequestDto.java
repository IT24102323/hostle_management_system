package com.hostel.management.dto.visit;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitRequestDto {

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    private String visitorContact;

    @Email(message = "Invalid email address")
    private String visitorEmail;

    private String preferredRoomType;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    private LocalDate visitDate;
    private String visitTime;
}
