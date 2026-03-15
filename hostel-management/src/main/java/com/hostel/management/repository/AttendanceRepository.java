package com.hostel.management.repository;

import com.hostel.management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByResidentId(Long residentId);
    List<Attendance> findByResidentIdAndDate(Long residentId, LocalDate date);
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
