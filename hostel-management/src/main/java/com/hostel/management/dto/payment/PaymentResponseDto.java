package com.hostel.management.dto.payment;

import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long residentId;
    private String residentName;
    private String roomNumber;
    private String month;
    private Double amount;
    private Double foodCharge;
    private Double lateFee;
    private Double total;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDate paymentDate;
    private LocalDate paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
