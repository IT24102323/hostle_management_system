package com.hostel.management.service.impl;

import com.hostel.management.dto.cleaning.RoomCleaningScheduleRequestDto;
import com.hostel.management.dto.cleaning.RoomCleaningScheduleResponseDto;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.RoomCleaningSchedule;
import com.hostel.management.enums.CleaningScheduleStatus;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.entity.Resident;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.repository.RoomCleaningScheduleRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.service.EmailService;
import com.hostel.management.service.RoomCleaningScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomCleaningScheduleServiceImpl implements RoomCleaningScheduleService {

    private final RoomCleaningScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final ResidentRepository residentRepository;
    private final EmailService emailService;

    public RoomCleaningScheduleServiceImpl(RoomCleaningScheduleRepository scheduleRepository,
                                           RoomRepository roomRepository,
                                           ResidentRepository residentRepository,
                                           EmailService emailService) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.residentRepository = residentRepository;
        this.emailService = emailService;
    }

    @Override
    public RoomCleaningScheduleResponseDto createSchedule(RoomCleaningScheduleRequestDto dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getRoomId()));

        LocalDate scheduledDate = LocalDate.parse(dto.getScheduledDate());
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        RoomCleaningSchedule schedule = RoomCleaningSchedule.builder()
                .room(room)
                .scheduledDate(scheduledDate)
                .weekNumber(scheduledDate.get(weekFields.weekOfWeekBasedYear()))
                .year(scheduledDate.getYear())
                .status(CleaningScheduleStatus.SCHEDULED)
                .assignedResidents(dto.getAssignedResidents())
                .notes(dto.getNotes())
                .build();

        RoomCleaningSchedule saved = scheduleRepository.save(schedule);

        // Notify all residents in the room via email
        try {
            List<Resident> residents = residentRepository.findByRoomId(room.getId());
            for (Resident resident : residents) {
                if (resident.getEmail() != null && !resident.getEmail().isBlank()) {
                    emailService.sendCleaningScheduleNotification(
                            resident.getEmail(), resident.getName(), saved);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send cleaning notification emails: " + e.getMessage());
        }

        return toDto(saved);
    }

    @Override
    public RoomCleaningScheduleResponseDto updateSchedule(Long id, RoomCleaningScheduleRequestDto dto) {
        RoomCleaningSchedule schedule = findById(id);

        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", dto.getRoomId()));
            schedule.setRoom(room);
        }
        if (dto.getScheduledDate() != null) {
            LocalDate scheduledDate = LocalDate.parse(dto.getScheduledDate());
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            schedule.setScheduledDate(scheduledDate);
            schedule.setWeekNumber(scheduledDate.get(weekFields.weekOfWeekBasedYear()));
            schedule.setYear(scheduledDate.getYear());
        }
        if (dto.getAssignedResidents() != null) {
            schedule.setAssignedResidents(dto.getAssignedResidents());
        }
        if (dto.getNotes() != null) {
            schedule.setNotes(dto.getNotes());
        }

        return toDto(scheduleRepository.save(schedule));
    }

    @Override
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("RoomCleaningSchedule", id);
        }
        scheduleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomCleaningScheduleResponseDto getScheduleById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCleaningScheduleResponseDto> getAllSchedules(Integer weekNumber, Integer year) {
        List<RoomCleaningSchedule> schedules;
        if (weekNumber != null && year != null) {
            schedules = scheduleRepository.findByWeekNumberAndYear(weekNumber, year);
        } else {
            schedules = scheduleRepository.findAll();
        }
        return schedules.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCleaningScheduleResponseDto> getSchedulesByRoom(Long roomId) {
        return scheduleRepository.findByRoomId(roomId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public RoomCleaningScheduleResponseDto markAsCompleted(Long id, String completedBy) {
        RoomCleaningSchedule schedule = findById(id);
        schedule.setStatus(CleaningScheduleStatus.COMPLETED);
        schedule.setCompletedAt(LocalDateTime.now());
        schedule.setCompletedBy(completedBy);
        return toDto(scheduleRepository.save(schedule));
    }

    @Override
    public RoomCleaningScheduleResponseDto updateStatus(Long id, String status) {
        RoomCleaningSchedule schedule = findById(id);
        schedule.setStatus(CleaningScheduleStatus.valueOf(status));
        if ("COMPLETED".equals(status)) {
            schedule.setCompletedAt(LocalDateTime.now());
        }
        return toDto(scheduleRepository.save(schedule));
    }

    private RoomCleaningSchedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomCleaningSchedule", id));
    }

    private RoomCleaningScheduleResponseDto toDto(RoomCleaningSchedule s) {
        return RoomCleaningScheduleResponseDto.builder()
                .id(s.getId())
                .roomId(s.getRoom().getId())
                .roomNumber(s.getRoom().getRoomNumber())
                .scheduledDate(s.getScheduledDate())
                .weekNumber(s.getWeekNumber())
                .year(s.getYear())
                .status(s.getStatus().name())
                .assignedResidents(s.getAssignedResidents())
                .notes(s.getNotes())
                .completedAt(s.getCompletedAt())
                .completedBy(s.getCompletedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
