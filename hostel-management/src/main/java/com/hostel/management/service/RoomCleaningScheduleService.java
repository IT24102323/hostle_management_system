package com.hostel.management.service;

import com.hostel.management.dto.cleaning.RoomCleaningScheduleRequestDto;
import com.hostel.management.dto.cleaning.RoomCleaningScheduleResponseDto;

import java.util.List;

public interface RoomCleaningScheduleService {

    RoomCleaningScheduleResponseDto createSchedule(RoomCleaningScheduleRequestDto requestDto);

    RoomCleaningScheduleResponseDto updateSchedule(Long id, RoomCleaningScheduleRequestDto requestDto);

    void deleteSchedule(Long id);

    RoomCleaningScheduleResponseDto getScheduleById(Long id);

    List<RoomCleaningScheduleResponseDto> getAllSchedules(Integer weekNumber, Integer year);

    List<RoomCleaningScheduleResponseDto> getSchedulesByRoom(Long roomId);

    RoomCleaningScheduleResponseDto markAsCompleted(Long id, String completedBy);

    RoomCleaningScheduleResponseDto updateStatus(Long id, String status);
}
