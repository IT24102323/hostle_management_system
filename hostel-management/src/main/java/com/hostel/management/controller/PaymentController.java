package com.hostel.management.controller;

import com.hostel.management.dto.payment.PaymentRequestDto;
import com.hostel.management.dto.payment.PaymentResponseDto;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponseDto>> createPayment(
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto payment = paymentService.createPayment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment record created.", payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> updatePayment(
            @PathVariable Long id,
            @RequestBody PaymentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Payment updated.",
                paymentService.updatePayment(id, requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved.",
                paymentService.getPaymentById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getAllPayments(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved.",
                paymentService.getAllPayments(residentId, status)));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) PaymentMethod method) {
        return ResponseEntity.ok(ApiResponse.success("Payment marked as paid.",
                paymentService.markAsPaid(id, method)));
    }

    @PostMapping("/pay-all")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> payAllPending(
            @RequestParam Long residentId,
            @RequestParam(required = false) PaymentMethod method) {
        return ResponseEntity.ok(ApiResponse.success("All pending payments marked as paid.",
                paymentService.payAllPending(residentId, method)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {
        return ResponseEntity.ok(ApiResponse.success("Payment stats retrieved.", paymentService.getPaymentStats()));
    }
}
