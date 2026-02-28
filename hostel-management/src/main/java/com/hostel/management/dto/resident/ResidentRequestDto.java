package com.hostel.management.dto.resident;

import com.hostel.management.enums.ResidentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String nic;

    @Pattern(regexp = "^[0-9]{3}-[0-9]{7}$|^[0-9]{10}$|^$",
             message = "Contact must be a valid phone number")
    private String contact;

    @Email(message = "Invalid email address")
    private String email;

    private String course;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private LocalDate joinDate;
    private Long roomId;
    private ResidentStatus status;
}
