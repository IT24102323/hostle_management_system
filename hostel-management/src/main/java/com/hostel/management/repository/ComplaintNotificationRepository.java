package com.hostel.management.repository;

import com.hostel.management.entity.ComplaintNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintNotificationRepository extends JpaRepository<ComplaintNotification, Long> {

    List<ComplaintNotification> findByResidentIdOrderByCreatedAtDesc(Long residentId);

    List<ComplaintNotification> findByResidentIdAndIsReadFalseOrderByCreatedAtDesc(Long residentId);

    long countByResidentIdAndIsReadFalse(Long residentId);

    List<ComplaintNotification> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    @Modifying
    @Query("UPDATE ComplaintNotification n SET n.isRead = true WHERE n.resident.id = :residentId AND n.isRead = false")
    void markAllReadForResident(Long residentId);
}
