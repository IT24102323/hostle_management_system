package com.hostel.management.controller;

import com.hostel.management.dto.resident.ResidentRequestDto;
import com.hostel.management.dto.resident.ResidentResponseDto;
import com.hostel.management.enums.ResidentStatus;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResidentResponseDto>> createResident(
            @Valid @RequestBody ResidentRequestDto requestDto) {
        ResidentResponseDto resident = residentService.createResident(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resident registered successfully.", resident));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResidentResponseDto>> updateResident(
            @PathVariable Long id,
            @Valid @RequestBody ResidentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Resident updated successfully.",
                residentService.updateResident(id, requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return ResponseEntity.ok(ApiResponse.success("Resident removed successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResidentResponseDto>> getResidentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Resident retrieved successfully.",
                residentService.getResidentById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResidentResponseDto>>> getAllResidents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ResidentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Residents retrieved successfully.",
                residentService.getAllResidents(name, status)));
    }

    @PutMapping("/{id}/assign-room/{roomId}")
    public ResponseEntity<ApiResponse<ResidentResponseDto>> assignRoom(
            @PathVariable Long id, @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success("Room assigned successfully.",
                residentService.assignRoom(id, roomId)));
    }

    @PutMapping("/{id}/remove-from-room")
    public ResponseEntity<ApiResponse<ResidentResponseDto>> removeFromRoom(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Resident removed from room.",
                residentService.removeFromRoom(id)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentStats() {
        return ResponseEntity.ok(ApiResponse.success("Resident stats retrieved.", residentService.getResidentStats()));
    }
}
