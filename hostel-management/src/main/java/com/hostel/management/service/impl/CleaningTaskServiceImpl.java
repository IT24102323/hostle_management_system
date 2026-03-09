package com.hostel.management.service.impl;

import com.hostel.management.dto.cleaning.CleaningTaskRequestDto;
import com.hostel.management.dto.cleaning.CleaningTaskResponseDto;
import com.hostel.management.entity.CleaningTask;
import com.hostel.management.entity.Resident;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.CleaningTaskRepository;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.service.CleaningTaskService;
import com.hostel.management.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CleaningTaskServiceImpl implements CleaningTaskService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final ResidentRepository residentRepository;
    private final EmailService emailService;

    public CleaningTaskServiceImpl(CleaningTaskRepository cleaningTaskRepository,
                                   ResidentRepository residentRepository,
                                   EmailService emailService) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.residentRepository = residentRepository;
        this.emailService = emailService;
    }

    @Override
    public CleaningTaskResponseDto createTask(CleaningTaskRequestDto dto) {
        CleaningTask task = CleaningTask.builder()
                .area(dto.getArea())
                .dayOfWeek(dto.getDayOfWeek())
                .timeSlot(dto.getTimeSlot())
                .assignedStaff(dto.getAssignedStaff())
                .assignedTo(dto.getAssignedTo())
                .scheduledDate(dto.getScheduledDate() != null ? LocalDate.parse(dto.getScheduledDate()) : null)
                .notes(dto.getNotes())
                .completionStatus(dto.getCompletionStatus() != null ? dto.getCompletionStatus() : "Pending")
                .build();
        CleaningTask saved = cleaningTaskRepository.save(task);

        // Send email notifications to assigned residents
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().isBlank()) {
            String[] names = dto.getAssignedTo().split(",");
            for (String name : names) {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) continue;
                List<Resident> matches = residentRepository.findByNameContainingIgnoreCase(trimmed);
                for (Resident r : matches) {
                    if (r.getEmail() != null && !r.getEmail().isBlank()) {
                        emailService.sendCleaningTaskNotification(r.getEmail(), r.getName(), saved);
                    }
                }
            }
        }

        return toDto(saved);
    }

    @Override
    public CleaningTaskResponseDto updateTask(Long id, CleaningTaskRequestDto dto) {
        CleaningTask task = findById(id);
        if (dto.getArea() != null) task.setArea(dto.getArea());
        if (dto.getDayOfWeek() != null) task.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getTimeSlot() != null) task.setTimeSlot(dto.getTimeSlot());
        if (dto.getAssignedStaff() != null) task.setAssignedStaff(dto.getAssignedStaff());
        if (dto.getAssignedTo() != null) task.setAssignedTo(dto.getAssignedTo());
        if (dto.getScheduledDate() != null) task.setScheduledDate(LocalDate.parse(dto.getScheduledDate()));
        if (dto.getNotes() != null) task.setNotes(dto.getNotes());
        if (dto.getCompletionStatus() != null) task.setCompletionStatus(dto.getCompletionStatus());
        return toDto(cleaningTaskRepository.save(task));
    }

    @Override
    public void deleteTask(Long id) {
        if (!cleaningTaskRepository.existsById(id)) {
            throw new ResourceNotFoundException("CleaningTask", id);
        }
        cleaningTaskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CleaningTaskResponseDto getTaskById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleaningTaskResponseDto> getAllTasks(String dayOfWeek) {
        List<CleaningTask> tasks = (dayOfWeek != null && !dayOfWeek.isBlank())
                ? cleaningTaskRepository.findByDayOfWeek(dayOfWeek)
                : cleaningTaskRepository.findAll();
        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CleaningTaskResponseDto markComplete(Long id, String completedBy) {
        CleaningTask task = findById(id);
        task.setCompletionStatus("Completed");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedBy(completedBy);
        return toDto(cleaningTaskRepository.save(task));
    }

    private CleaningTask findById(Long id) {
        return cleaningTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CleaningTask", id));
    }

    private CleaningTaskResponseDto toDto(CleaningTask t) {
        return CleaningTaskResponseDto.builder()
                .id(t.getId())
                .area(t.getArea())
                .dayOfWeek(t.getDayOfWeek())
                .timeSlot(t.getTimeSlot())
                .assignedStaff(t.getAssignedStaff())
                .assignedTo(t.getAssignedTo())
                .scheduledDate(t.getScheduledDate())
                .notes(t.getNotes())
                .completionStatus(t.getCompletionStatus())
                .completedAt(t.getCompletedAt())
                .completedBy(t.getCompletedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
