package com.hostel.management.dto.payment;

import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "Resident ID is required")
    private Long residentId;

    @NotBlank(message = "Month is required")
    private String month;

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount must be zero or positive")
    private Double amount;

    @PositiveOrZero(message = "Food charge must be zero or positive")
    private Double foodCharge;

    @PositiveOrZero(message = "Late fee must be zero or positive")
    private Double lateFee;

    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDate paymentDate;
    private LocalDate paidDate;
}
