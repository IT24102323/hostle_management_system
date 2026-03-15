package com.hostel.management.service;

import com.hostel.management.dto.payment.PaymentRequestDto;
import com.hostel.management.dto.payment.PaymentResponseDto;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Payment and Fee management operations.
 */
public interface PaymentService {

    /** Create a new payment record. */
    PaymentResponseDto createPayment(PaymentRequestDto requestDto);

    /** Update an existing payment (status, method, paidDate). */
    PaymentResponseDto updatePayment(Long id, PaymentRequestDto requestDto);

    /** Delete a payment record. */
    void deletePayment(Long id);

    /** Get a payment by ID. */
    PaymentResponseDto getPaymentById(Long id);

    /** Get all payments with optional residentId or status filters. */
    List<PaymentResponseDto> getAllPayments(Long residentId, PaymentStatus status);

    /** Mark a single payment as paid. */
    PaymentResponseDto markAsPaid(Long id, PaymentMethod method);

    /** Bulk-pay all pending payments for a resident. */
    List<PaymentResponseDto> payAllPending(Long residentId, PaymentMethod method);

    /** Get dashboard payment stats. */
    Map<String, Object> getPaymentStats();
}
