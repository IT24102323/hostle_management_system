package com.hostel.management.repository;

import com.hostel.management.entity.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByDayOfWeek(String dayOfWeek);

    List<CleaningTask> findByAssignedStaffContainingIgnoreCase(String staff);

    List<CleaningTask> findByCompletionStatus(String completionStatus);
}
