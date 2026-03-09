package com.hostel.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cleaning_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private String dayOfWeek;

    @Column(nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String assignedStaff;

    private String assignedTo;

    private LocalDate scheduledDate;

    private String notes;

    private String completionStatus = "Pending";

    private LocalDateTime completedAt;

    private String completedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (completionStatus == null) completionStatus = "Pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
