package com.hostel.management.controller;

import com.hostel.management.dto.visit.VisitRequestDto;
import com.hostel.management.dto.visit.VisitResponseDto;
import com.hostel.management.enums.VisitStatus;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VisitResponseDto>> createVisit(
            @Valid @RequestBody VisitRequestDto requestDto) {
        VisitResponseDto visit = visitService.createVisit(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Visit request submitted.", visit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitResponseDto>> updateVisit(
            @PathVariable Long id,
            @RequestBody VisitRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Visit updated.",
                visitService.updateVisit(id, requestDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.ok(ApiResponse.success("Visit deleted."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitResponseDto>> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Visit retrieved.",
                visitService.getVisitById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitResponseDto>>> getAllVisits(
            @RequestParam(required = false) VisitStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Visits retrieved.",
                visitService.getAllVisits(status)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VisitResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam VisitStatus status,
            @RequestParam(required = false) String adminNotes) {
        return ResponseEntity.ok(ApiResponse.success("Visit status updated.",
                visitService.updateStatus(id, status, adminNotes)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVisitStats() {
        return ResponseEntity.ok(ApiResponse.success("Visit stats retrieved.", visitService.getVisitStats()));
    }
}
