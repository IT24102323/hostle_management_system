package com.hostel.management.dto.complaint;

import com.hostel.management.enums.ComplaintPriority;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintRequestDto {

    private Long residentId;   // required on create, optional on update

    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String category;

    /**
     * When null on create, the service auto-assigns priority based on title/category.
     */
    private ComplaintPriority priority;
}
