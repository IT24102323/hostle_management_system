package com.hostel.management.repository;

import com.hostel.management.entity.RoomCleaningSchedule;
import com.hostel.management.enums.CleaningScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomCleaningScheduleRepository extends JpaRepository<RoomCleaningSchedule, Long> {

    List<RoomCleaningSchedule> findByWeekNumberAndYear(Integer weekNumber, Integer year);

    List<RoomCleaningSchedule> findByRoomId(Long roomId);

    List<RoomCleaningSchedule> findByStatus(CleaningScheduleStatus status);

    List<RoomCleaningSchedule> findByScheduledDateBetween(LocalDate start, LocalDate end);

    List<RoomCleaningSchedule> findByRoomIdAndWeekNumberAndYear(Long roomId, Integer weekNumber, Integer year);
}
