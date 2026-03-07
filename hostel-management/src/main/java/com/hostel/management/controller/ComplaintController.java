package com.hostel.management.controller;

import com.hostel.management.dto.complaint.ComplaintNotificationDto;
import com.hostel.management.dto.complaint.ComplaintRequestDto;
import com.hostel.management.dto.complaint.ComplaintResponseDto;
import com.hostel.management.dto.complaint.ComplaintStatusUpdateDto;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> createComplaint(
            @Valid @RequestBody ComplaintRequestDto requestDto) {
        ComplaintResponseDto complaint = complaintService.createComplaint(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully.", complaint));
    }

    /** Student can update a PENDING complaint (title, description, category, priority). */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> updateComplaint(
            @PathVariable Long id,
            @RequestBody ComplaintRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Complaint updated.",
                complaintService.updateComplaint(id, requestDto)));
    }

    /** Student can delete a PENDING complaint. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved.",
                complaintService.getComplaintById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponseDto>>> getAllComplaints(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) ComplaintStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved.",
                complaintService.getAllComplaints(residentId, status)));
    }

    // ── STATUS UPDATE (admin) ────────────────────────────────────────────────

    /**
     * Admin updates status with optional resolution note and admin note.
     * Triggers a notification for the resident.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody ComplaintStatusUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated.",
                complaintService.updateStatus(id, dto)));
    }

    // ── REMINDER ────────────────────────────────────────────────────────────

    /** Student sends a reminder for an IN_PROGRESS / WILL_TAKE_ACTION complaint. */
    @PostMapping("/{id}/reminder")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> sendReminder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Reminder sent successfully.",
                complaintService.sendReminder(id)));
    }

    // ── NOTIFICATIONS ────────────────────────────────────────────────────────

    @GetMapping("/notifications/resident/{residentId}")
    public ResponseEntity<ApiResponse<List<ComplaintNotificationDto>>> getNotifications(
            @PathVariable Long residentId) {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved.",
                complaintService.getNotificationsForResident(residentId)));
    }

    @GetMapping("/notifications/resident/{residentId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread(@PathVariable Long residentId) {
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved.",
                complaintService.countUnreadNotifications(residentId)));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<?>> markRead(@PathVariable Long notificationId) {
        complaintService.markNotificationRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read."));
    }

    @PutMapping("/notifications/resident/{residentId}/read-all")
    public ResponseEntity<ApiResponse<?>> markAllRead(@PathVariable Long residentId) {
        complaintService.markAllNotificationsRead(residentId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read."));
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComplaintStats() {
        return ResponseEntity.ok(ApiResponse.success("Complaint stats retrieved.",
                complaintService.getComplaintStats()));
    }
}

