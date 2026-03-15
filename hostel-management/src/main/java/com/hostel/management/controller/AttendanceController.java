package com.hostel.management.controller;

import com.hostel.management.dto.attendance.AttendanceRequestDto;
import com.hostel.management.dto.attendance.AttendanceResponseDto;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> createAttendance(@RequestBody AttendanceRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Attendance marked.", attendanceService.createAttendance(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> updateAttendance(
            @PathVariable Long id, @RequestBody AttendanceRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Attendance updated.", attendanceService.updateAttendance(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance deleted."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getAllAttendance() {
        return ResponseEntity.ok(ApiResponse.success("Attendance retrieved.", attendanceService.getAllAttendance()));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Attendance retrieved.", attendanceService.getAttendanceByDate(date)));
    }

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getAttendanceByResident(@PathVariable Long residentId) {
        return ResponseEntity.ok(ApiResponse.success("Attendance retrieved.", attendanceService.getAttendanceByResident(residentId)));
    }

    @PostMapping("/mark/{residentId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> markAttendance(
            @PathVariable Long residentId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Attendance marked.", attendanceService.markAttendance(residentId, status)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved.", attendanceService.getAttendanceStats()));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getTodayAttendance() {
        return ResponseEntity.ok(ApiResponse.success("Today's attendance.", attendanceService.getAttendanceByDate(LocalDate.now())));
    }
}
