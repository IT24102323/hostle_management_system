package com.hostel.management.controller;

import com.hostel.management.dto.cleaning.RoomCleaningScheduleRequestDto;
import com.hostel.management.dto.cleaning.RoomCleaningScheduleResponseDto;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.RoomCleaningScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-cleaning")
public class RoomCleaningScheduleController {

    private final RoomCleaningScheduleService service;

    public RoomCleaningScheduleController(RoomCleaningScheduleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomCleaningScheduleResponseDto>> create(
            @Valid @RequestBody RoomCleaningScheduleRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room cleaning schedule created.", service.createSchedule(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomCleaningScheduleResponseDto>> update(
            @PathVariable Long id, @RequestBody RoomCleaningScheduleRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Schedule updated.", service.updateSchedule(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        service.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule deleted."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomCleaningScheduleResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Schedule retrieved.", service.getScheduleById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomCleaningScheduleResponseDto>>> getAll(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Schedules retrieved.", service.getAllSchedules(week, year)));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<RoomCleaningScheduleResponseDto>>> getByRoom(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success("Room schedules retrieved.", service.getSchedulesByRoom(roomId)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<RoomCleaningScheduleResponseDto>> markComplete(
            @PathVariable Long id, @RequestParam(defaultValue = "") String completedBy) {
        return ResponseEntity.ok(ApiResponse.success("Marked as completed.", service.markAsCompleted(id, completedBy)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RoomCleaningScheduleResponseDto>> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated.", service.updateStatus(id, status)));
    }
}
