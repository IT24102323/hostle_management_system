package com.hostel.management.repository;

import com.hostel.management.entity.Visit;
import com.hostel.management.enums.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByStatus(VisitStatus status);

    List<Visit> findByVisitorNameContainingIgnoreCase(String name);

    List<Visit> findByVisitDate(LocalDate visitDate);

    long countByStatus(VisitStatus status);
}
