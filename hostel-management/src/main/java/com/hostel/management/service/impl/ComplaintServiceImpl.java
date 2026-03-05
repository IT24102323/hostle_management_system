package com.hostel.management.service.impl;

import com.hostel.management.dto.complaint.ComplaintNotificationDto;
import com.hostel.management.dto.complaint.ComplaintRequestDto;
import com.hostel.management.dto.complaint.ComplaintResponseDto;
import com.hostel.management.dto.complaint.ComplaintStatusUpdateDto;
import com.hostel.management.entity.Complaint;
import com.hostel.management.entity.ComplaintNotification;
import com.hostel.management.entity.Resident;
import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.ComplaintNotificationRepository;
import com.hostel.management.repository.ComplaintRepository;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.service.ComplaintService;
import com.hostel.management.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ComplaintService}.
 * Handles complaint lifecycle, auto-priority assignment, reminders, and notifications.
 */
@Service
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ResidentRepository residentRepository;
    private final ComplaintNotificationRepository notificationRepository;
    private final EmailService emailService;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                ResidentRepository residentRepository,
                                ComplaintNotificationRepository notificationRepository,
                                EmailService emailService) {
        this.complaintRepository = complaintRepository;
        this.residentRepository = residentRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    @Override
    public ComplaintResponseDto createComplaint(ComplaintRequestDto dto) {
        if (dto.getResidentId() == null) {
            throw new IllegalArgumentException("Resident ID is required when creating a complaint.");
        }
        Resident resident = findResidentById(dto.getResidentId());

        ComplaintPriority priority = dto.getPriority() != null
                ? dto.getPriority()
                : autoAssignPriority(dto.getTitle(), dto.getCategory());

        Complaint complaint = Complaint.builder()
                .resident(resident)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .priority(priority)
                .status(ComplaintStatus.PENDING)
                .complaintDate(LocalDate.now())
                .reminderCount(0)
                .build();

        return toDto(complaintRepository.save(complaint), resident.getId());
    }

    // ── UPDATE (by student – PENDING only) ─────────────────────────────────

    @Override
    public ComplaintResponseDto updateComplaint(Long id, ComplaintRequestDto dto) {
        Complaint complaint = findById(id);

        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalStateException(
                    "Complaint can only be edited while it is in PENDING status.");
        }

        if (dto.getTitle() != null) complaint.setTitle(dto.getTitle());
        if (dto.getDescription() != null) complaint.setDescription(dto.getDescription());
        if (dto.getCategory() != null) complaint.setCategory(dto.getCategory());

        // Re-run auto-priority if priority not explicitly provided
        if (dto.getPriority() != null) {
            complaint.setPriority(dto.getPriority());
        } else {
            complaint.setPriority(autoAssignPriority(complaint.getTitle(), complaint.getCategory()));
        }

        return toDto(complaintRepository.save(complaint), complaint.getResident().getId());
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    @Override
    public void deleteComplaint(Long id) {
        Complaint complaint = findById(id);
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalStateException(
                    "Complaint can only be deleted while it is in PENDING status.");
        }
        // Delete all notifications associated with this complaint first
        notificationRepository.findByComplaintIdOrderByCreatedAtDesc(id)
                .forEach(notificationRepository::delete);
        complaintRepository.deleteById(id);
    }

    // ── READ ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponseDto getComplaintById(Long id) {
        Complaint c = findById(id);
        return toDto(c, c.getResident().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getAllComplaints(Long residentId, ComplaintStatus status) {
        List<Complaint> complaints;
        if (residentId != null && status != null) {
            complaints = complaintRepository.findByResidentIdAndStatus(residentId, status);
        } else if (residentId != null) {
            complaints = complaintRepository.findByResidentIdOrderByCreatedAtDesc(residentId);
        } else if (status != null) {
            complaints = complaintRepository.findByStatus(status);
        } else {
            complaints = complaintRepository.findAll();
        }
        Long filterResidentId = residentId;
        return complaints.stream()
                .map(c -> toDto(c, filterResidentId != null ? filterResidentId : c.getResident().getId()))
                .collect(Collectors.toList());
    }

    // ── STATUS UPDATE (by admin) ────────────────────────────────────────────

    @Override
    public ComplaintResponseDto updateStatus(Long id, ComplaintStatusUpdateDto dto) {
        Complaint complaint = findById(id);
        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = dto.getStatus();

        complaint.setStatus(newStatus);
        if (dto.getResolution() != null) complaint.setResolution(dto.getResolution());
        if (dto.getAdminNote() != null) complaint.setAdminNote(dto.getAdminNote());
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedDate(LocalDate.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        // Create a STATUS_UPDATE notification for the resident
        String message = buildStatusUpdateMessage(complaint.getTitle(), oldStatus, newStatus, dto.getAdminNote());
        createNotification(saved, saved.getResident(), "STATUS_UPDATE", message);

        // Send email to resident
        try { emailService.sendComplaintStatusUpdateEmail(saved); } catch (Exception ignored) {}

        return toDto(saved, saved.getResident().getId());
    }

    // ── REMINDER ────────────────────────────────────────────────────────────

    @Override
    public ComplaintResponseDto sendReminder(Long complaintId) {
        Complaint complaint = findById(complaintId);
        ComplaintStatus status = complaint.getStatus();

        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED) {
            throw new IllegalStateException(
                    "Cannot send a reminder for a complaint that is already " + status + ".");
        }

        complaint.setReminderCount(complaint.getReminderCount() == null ? 1 : complaint.getReminderCount() + 1);
        complaint.setLastReminderAt(LocalDateTime.now());
        Complaint saved = complaintRepository.save(complaint);

        String message = "Reminder #" + saved.getReminderCount()
                + " sent for complaint: \"" + complaint.getTitle() + "\". Please take action.";
        createNotification(saved, saved.getResident(), "REMINDER", message);

        // Send reminder email to admin
        try { emailService.sendReminderEmailToAdmin(saved); } catch (Exception ignored) {}

        return toDto(saved, saved.getResident().getId());
    }

    // ── NOTIFICATIONS ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintNotificationDto> getNotificationsForResident(Long residentId) {
        return notificationRepository
                .findByResidentIdOrderByCreatedAtDesc(residentId)
                .stream()
                .map(this::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long residentId) {
        return notificationRepository.countByResidentIdAndIsReadFalse(residentId);
    }

    @Override
    public void markNotificationRead(Long notificationId) {
        ComplaintNotification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    public void markAllNotificationsRead(Long residentId) {
        notificationRepository.markAllReadForResident(residentId);
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getComplaintStats() {
        return Map.of(
                "total", complaintRepository.count(),
                "pending", complaintRepository.countByStatus(ComplaintStatus.PENDING),
                "inProgress", complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS),
                "willTakeAction", complaintRepository.countByStatus(ComplaintStatus.WILL_TAKE_ACTION),
                "resolved", complaintRepository.countByStatus(ComplaintStatus.RESOLVED),
                "rejected", complaintRepository.countByStatus(ComplaintStatus.REJECTED)
        );
    }

    // ── AUTO-PRIORITY LOGIC ──────────────────────────────────────────────────

    /**
     * Assigns priority based on category and title keywords.
     * HIGH  : Electrical, Security, or keywords: fire/emergency/danger/urgent/broken/flood/leak
     * MEDIUM: Plumbing, Internet, or keywords: not working/repair/fix/water
     * LOW   : everything else
     */
    private ComplaintPriority autoAssignPriority(String title, String category) {
        String text = ((title == null ? "" : title) + " " + (category == null ? "" : category)).toLowerCase();

        // High-priority categories
        if (category != null) {
            String cat = category.toLowerCase();
            if (cat.contains("electrical") || cat.contains("security")) {
                return ComplaintPriority.HIGH;
            }
            if (cat.contains("plumbing") || cat.contains("internet") || cat.contains("wifi")) {
                return ComplaintPriority.MEDIUM;
            }
        }

        // High-priority keywords in title
        if (text.matches(".*(fire|emergency|danger|urgent|broken|flood|leak|electric shock|power cut).*")) {
            return ComplaintPriority.HIGH;
        }

        // Medium-priority keywords
        if (text.matches(".*(not working|repair|fix|water|pipe|wifi|internet|noise|pest).*")) {
            return ComplaintPriority.MEDIUM;
        }

        return ComplaintPriority.LOW;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Complaint findById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", id));
    }

    private Resident findResidentById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", id));
    }

    private void createNotification(Complaint complaint, Resident resident, String type, String message) {
        ComplaintNotification notification = ComplaintNotification.builder()
                .complaint(complaint)
                .resident(resident)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private String buildStatusUpdateMessage(String title, ComplaintStatus oldStatus,
                                            ComplaintStatus newStatus, String adminNote) {
        String statusLabel = switch (newStatus) {
            case IN_PROGRESS -> "is now In Progress";
            case WILL_TAKE_ACTION -> "has been noted – action will be taken";
            case RESOLVED -> "has been resolved";
            case REJECTED -> "has been rejected";
            default -> "status updated to " + newStatus;
        };
        String msg = "Your complaint \"" + title + "\" " + statusLabel + ".";
        if (adminNote != null && !adminNote.isBlank()) {
            msg += " Admin note: " + adminNote;
        }
        return msg;
    }

    private ComplaintResponseDto toDto(Complaint c, Long residentIdForNotifications) {
        long unread = 0;
        if (residentIdForNotifications != null) {
            unread = notificationRepository.countByResidentIdAndIsReadFalse(residentIdForNotifications);
        }

        ComplaintStatus status = c.getStatus();
        boolean reminderAllowed = status != ComplaintStatus.RESOLVED
                && status != ComplaintStatus.REJECTED;
        boolean editAllowed = status == ComplaintStatus.PENDING;
        boolean deleteAllowed = status == ComplaintStatus.PENDING;

        return ComplaintResponseDto.builder()
                .id(c.getId())
                .residentId(c.getResident().getId())
                .residentName(c.getResident().getName())
                .roomNumber(c.getResident().getRoom() != null ? c.getResident().getRoom().getRoomNumber() : null)
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .priority(c.getPriority())
                .status(c.getStatus())
                .resolution(c.getResolution())
                .adminNote(c.getAdminNote())
                .reminderCount(c.getReminderCount() != null ? c.getReminderCount() : 0)
                .lastReminderAt(c.getLastReminderAt())
                .reminderAllowed(reminderAllowed)
                .editAllowed(editAllowed)
                .deleteAllowed(deleteAllowed)
                .unreadNotifications(unread)
                .complaintDate(c.getComplaintDate())
                .resolvedDate(c.getResolvedDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ComplaintNotificationDto toNotificationDto(ComplaintNotification n) {
        return ComplaintNotificationDto.builder()
                .id(n.getId())
                .complaintId(n.getComplaint().getId())
                .complaintTitle(n.getComplaint().getTitle())
                .residentId(n.getResident().getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

