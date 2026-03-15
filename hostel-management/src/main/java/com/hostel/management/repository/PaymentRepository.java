package com.hostel.management.repository;

import com.hostel.management.entity.Payment;
import com.hostel.management.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByResidentId(Long residentId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByResidentIdAndStatus(Long residentId, PaymentStatus status);

    boolean existsByResidentIdAndMonth(Long residentId, String month);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Payment p WHERE p.status = :status")
    Double sumTotalByStatus(@Param("status") PaymentStatus status);

    long countByStatus(PaymentStatus status);
}
