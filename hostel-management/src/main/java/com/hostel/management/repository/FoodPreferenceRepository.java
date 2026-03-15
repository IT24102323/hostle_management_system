package com.hostel.management.repository;

import com.hostel.management.entity.FoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodPreferenceRepository extends JpaRepository<FoodPreference, Long> {
    List<FoodPreference> findByDate(LocalDate date);
    List<FoodPreference> findByResidentId(Long residentId);
    Optional<FoodPreference> findByResidentIdAndDate(Long residentId, LocalDate date);
    List<FoodPreference> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
