package com.hostel.management.controller;

import com.hostel.management.dto.cleaning.CleaningTaskRequestDto;
import com.hostel.management.dto.cleaning.CleaningTaskResponseDto;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.CleaningTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning")
public class CleaningController {

    private final CleaningTaskService cleaningTaskService;

    public CleaningController(CleaningTaskService cleaningTaskService) {
        this.cleaningTaskService = cleaningTaskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CleaningTaskResponseDto>> createTask(
            @Valid @RequestBody CleaningTaskRequestDto requestDto) {
        CleaningTaskResponseDto task = cleaningTaskService.createTask(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cleaning task created.", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CleaningTaskResponseDto>> updateTask(
            @PathVariable Long id,
            @RequestBody CleaningTaskRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Cleaning task updated.",
                cleaningTaskService.updateTask(id, requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteTask(@PathVariable Long id) {
        cleaningTaskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Cleaning task deleted."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CleaningTaskResponseDto>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Cleaning task retrieved.",
                cleaningTaskService.getTaskById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CleaningTaskResponseDto>>> getAllTasks(
            @RequestParam(required = false) String dayOfWeek) {
        return ResponseEntity.ok(ApiResponse.success("Cleaning tasks retrieved.",
                cleaningTaskService.getAllTasks(dayOfWeek)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CleaningTaskResponseDto>> markComplete(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Admin") String completedBy) {
        return ResponseEntity.ok(ApiResponse.success("Cleaning task marked as completed.",
                cleaningTaskService.markComplete(id, completedBy)));
    }
}
