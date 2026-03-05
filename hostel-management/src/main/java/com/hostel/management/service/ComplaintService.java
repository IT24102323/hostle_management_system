package com.hostel.management.service;

import com.hostel.management.dto.complaint.ComplaintNotificationDto;
import com.hostel.management.dto.complaint.ComplaintRequestDto;
import com.hostel.management.dto.complaint.ComplaintResponseDto;
import com.hostel.management.dto.complaint.ComplaintStatusUpdateDto;
import com.hostel.management.enums.ComplaintStatus;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Complaint and Maintenance management operations.
 */
public interface ComplaintService {

    /** Submit a new complaint. Priority is auto-assigned from title/category if not provided. */
    ComplaintResponseDto createComplaint(ComplaintRequestDto requestDto);

    /**
     * Update complaint details (title, description, category, priority).
     * Students may only update complaints in PENDING status.
     */
    ComplaintResponseDto updateComplaint(Long id, ComplaintRequestDto requestDto);

    /**
     * Delete a complaint. Students may only delete PENDING complaints.
     */
    void deleteComplaint(Long id);

    /** Get a complaint by ID. */
    ComplaintResponseDto getComplaintById(Long id);

    /** Get all complaints with optional residentId or status filters. */
    List<ComplaintResponseDto> getAllComplaints(Long residentId, ComplaintStatus status);

    /**
     * Admin: Update only the status (and optionally resolution note + admin note).
     * Creates a STATUS_UPDATE notification for the resident.
     */
    ComplaintResponseDto updateStatus(Long id, ComplaintStatusUpdateDto dto);

    /**
     * Legacy overload kept for backward compatibility.
     */
    default ComplaintResponseDto updateStatus(Long id, ComplaintStatus status, String resolution) {
        ComplaintStatusUpdateDto dto = new ComplaintStatusUpdateDto(status, resolution, null);
        return updateStatus(id, dto);
    }

    /** Resident sends a reminder for a complaint that is IN_PROGRESS or WILL_TAKE_ACTION. */
    ComplaintResponseDto sendReminder(Long complaintId);

    /** Get notifications for a resident (newest first). */
    List<ComplaintNotificationDto> getNotificationsForResident(Long residentId);

    /** Count unread notifications for a resident. */
    long countUnreadNotifications(Long residentId);

    /** Mark a single notification as read. */
    void markNotificationRead(Long notificationId);

    /** Mark all notifications as read for a resident. */
    void markAllNotificationsRead(Long residentId);

    /** Get complaint stats. */
    Map<String, Object> getComplaintStats();
}
