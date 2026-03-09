package com.hostel.management.service;

import com.hostel.management.dto.cleaning.CleaningTaskRequestDto;
import com.hostel.management.dto.cleaning.CleaningTaskResponseDto;

import java.util.List;

/**
 * Service interface for Cleaning Task / Attendance management operations.
 */
public interface CleaningTaskService {

    /** Create a new cleaning task. */
    CleaningTaskResponseDto createTask(CleaningTaskRequestDto requestDto);

    /** Update an existing cleaning task. */
    CleaningTaskResponseDto updateTask(Long id, CleaningTaskRequestDto requestDto);

    /** Delete a cleaning task. */
    void deleteTask(Long id);

    /** Get a task by ID. */
    CleaningTaskResponseDto getTaskById(Long id);

    /** Get all tasks with optional dayOfWeek filter. */
    List<CleaningTaskResponseDto> getAllTasks(String dayOfWeek);

    /** Mark a cleaning task as completed. */
    CleaningTaskResponseDto markComplete(Long id, String completedBy);
}
