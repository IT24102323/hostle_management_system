package com.hostel.management.service.impl;

import com.hostel.management.dto.payment.PaymentRequestDto;
import com.hostel.management.dto.payment.PaymentResponseDto;
import com.hostel.management.entity.Payment;
import com.hostel.management.entity.Resident;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PaymentService} containing all payment business logic.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ResidentRepository residentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ResidentRepository residentRepository) {
        this.paymentRepository = paymentRepository;
        this.residentRepository = residentRepository;
    }

    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto dto) {
        Resident resident = findResidentById(dto.getResidentId());

        if (paymentRepository.existsByResidentIdAndMonth(dto.getResidentId(), dto.getMonth())) {
            throw new BadRequestException("A payment for resident '" + resident.getName()
                    + "' for month '" + dto.getMonth() + "' already exists.");
        }

        double amount = dto.getAmount() != null ? dto.getAmount() : 0.0;
        double food = dto.getFoodCharge() != null ? dto.getFoodCharge() : 0.0;
        double late = dto.getLateFee() != null ? dto.getLateFee() : 0.0;

        Payment payment = Payment.builder()
                .resident(resident)
                .month(dto.getMonth())
                .amount(amount)
                .foodCharge(food)
                .lateFee(late)
                .total(amount + food + late)
                .status(dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PENDING)
                .method(dto.getMethod() != null ? dto.getMethod() : PaymentMethod.CASH)
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now())
                .paidDate(dto.getPaidDate())
                .build();

        return toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto updatePayment(Long id, PaymentRequestDto dto) {
        Payment payment = findById(id);

        if (dto.getAmount() != null) payment.setAmount(dto.getAmount());
        if (dto.getFoodCharge() != null) payment.setFoodCharge(dto.getFoodCharge());
        if (dto.getLateFee() != null) payment.setLateFee(dto.getLateFee());
        if (dto.getStatus() != null) payment.setStatus(dto.getStatus());
        if (dto.getMethod() != null) payment.setMethod(dto.getMethod());
        if (dto.getPaidDate() != null) payment.setPaidDate(dto.getPaidDate());

        // Recalculate total
        payment.setTotal(payment.getAmount() + payment.getFoodCharge() + payment.getLateFee());

        // Auto-set paidDate when marking as paid
        if (dto.getStatus() == PaymentStatus.PAID && payment.getPaidDate() == null) {
            payment.setPaidDate(LocalDate.now());
        }

        return toDto(paymentRepository.save(payment));
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", id);
        }
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPayments(Long residentId, PaymentStatus status) {
        List<Payment> payments;
        if (residentId != null && status != null) {
            payments = paymentRepository.findByResidentIdAndStatus(residentId, status);
        } else if (residentId != null) {
            payments = paymentRepository.findByResidentId(residentId);
        } else if (status != null) {
            payments = paymentRepository.findByStatus(status);
        } else {
            payments = paymentRepository.findAll();
        }
        return payments.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDto markAsPaid(Long id, PaymentMethod method) {
        Payment payment = findById(id);
        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod(method != null ? method : PaymentMethod.CASH);
        payment.setPaidDate(LocalDate.now());
        return toDto(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponseDto> payAllPending(Long residentId, PaymentMethod method) {
        List<Payment> pending = paymentRepository.findByResidentIdAndStatus(residentId, PaymentStatus.PENDING);
        if (pending.isEmpty()) {
            throw new BadRequestException("No pending payments found for this resident.");
        }
        LocalDate today = LocalDate.now();
        pending.forEach(p -> {
            p.setStatus(PaymentStatus.PAID);
            p.setMethod(method != null ? method : PaymentMethod.CASH);
            p.setPaidDate(today);
        });
        return paymentRepository.saveAll(pending).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStats() {
        Double totalPaid = paymentRepository.sumTotalByStatus(PaymentStatus.PAID);
        Double totalPending = paymentRepository.sumTotalByStatus(PaymentStatus.PENDING);
        return Map.of(
                "totalPaid", totalPaid != null ? totalPaid : 0.0,
                "totalPending", totalPending != null ? totalPending : 0.0,
                "pendingCount", paymentRepository.countByStatus(PaymentStatus.PENDING),
                "lateCount", paymentRepository.countByStatus(PaymentStatus.LATE)
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private Resident findResidentById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", id));
    }

    private PaymentResponseDto toDto(Payment p) {
        return PaymentResponseDto.builder()
                .id(p.getId())
                .residentId(p.getResident().getId())
                .residentName(p.getResident().getName())
                .roomNumber(p.getResident().getRoom() != null ? p.getResident().getRoom().getRoomNumber() : null)
                .month(p.getMonth())
                .amount(p.getAmount())
                .foodCharge(p.getFoodCharge())
                .lateFee(p.getLateFee())
                .total(p.getTotal())
                .status(p.getStatus())
                .method(p.getMethod())
                .paymentDate(p.getPaymentDate())
                .paidDate(p.getPaidDate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
