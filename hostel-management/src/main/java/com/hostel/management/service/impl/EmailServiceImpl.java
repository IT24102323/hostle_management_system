package com.hostel.management.service.impl;

import com.hostel.management.entity.Complaint;
import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends verification, complaint status update, and reminder emails.
 * Falls back to console log if mail is not configured.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String fromEmail;
    private final String adminEmail;

    public EmailServiceImpl(
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${spring.mail.username:}") String fromEmail,
            @Value("${app.admin-email:hasindutwm@gmail.com}") String adminEmail,
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.fromEmail = fromEmail;
        this.adminEmail = adminEmail;
        this.mailSender = mailSender;
    }

    // ── Verification ──────────────────────────────────────────────────────────

    @Override
    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        String verifyLink = baseUrl + "/verify.html?token=" + verificationToken;

        if (mailSender != null && fromEmail != null && !fromEmail.isBlank()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(toEmail);
                msg.setSubject("Verify your HostelHub account");
                msg.setText("Hi " + (name != null ? name : "there") + ",\n\n" +
                        "Please verify your email by clicking the link below:\n\n" +
                        verifyLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "If you did not register, please ignore this email.\n\n" +
                        "— HostelHub");
                mailSender.send(msg);
            } catch (Exception e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
                logVerificationLink(toEmail, verifyLink);
            }
        } else {
            logVerificationLink(toEmail, verifyLink);
        }
    }

    // ── Complaint Status Update → Resident ────────────────────────────────────

    @Override
    public void sendComplaintStatusUpdateEmail(Complaint complaint) {
        String toEmail = complaint.getResident().getEmail();
        if (toEmail == null || toEmail.isBlank()) return;

        String residentName = complaint.getResident().getName();
        String subject = "Your Complaint Has Been Updated - HostelHub";
        String html = buildStatusUpdateHtml(residentName, complaint);

        sendHtml(toEmail, subject, html, "complaint status update");
    }

    // ── Reminder → Admin ──────────────────────────────────────────────────────

    @Override
    public void sendReminderEmailToAdmin(Complaint complaint) {
        if (adminEmail == null || adminEmail.isBlank()) return;

        String residentName = complaint.getResident().getName();
        String subject = "[" + complaint.getPriority() + " PRIORITY] Complaint Reminder #"
                + complaint.getReminderCount() + " - " + complaint.getTitle();
        String html = buildReminderHtml(residentName, complaint);

        sendHtml(adminEmail, subject, html, "reminder to admin");
    }

    // ── HTML Builders ─────────────────────────────────────────────────────────

    private String buildStatusUpdateHtml(String residentName, Complaint complaint) {
        String statusColor = statusColor(complaint.getStatus());
        String statusLabel = statusLabel(complaint.getStatus());
        String priorityColor = priorityColor(complaint.getPriority());

        String resolutionSection = "";
        if (complaint.getResolution() != null && !complaint.getResolution().isBlank()) {
            resolutionSection = "<tr><td style='padding:8px 0;color:#64748b;font-weight:600;'>Resolution</td>"
                    + "<td style='padding:8px 0;color:#0f172a;'>" + escHtml(complaint.getResolution()) + "</td></tr>";
        }
        String adminNoteSection = "";
        if (complaint.getAdminNote() != null && !complaint.getAdminNote().isBlank()) {
            adminNoteSection = "<tr><td style='padding:8px 0;color:#64748b;font-weight:600;'>Admin Note</td>"
                    + "<td style='padding:8px 0;color:#0f172a;'>" + escHtml(complaint.getAdminNote()) + "</td></tr>";
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;"
                + "background:#f1f5f9;font-family:Inter,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;"
                + "overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#6366f1,#ec4899);padding:32px 40px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:700;'>🏠 HostelHub</h1>"
                + "<p style='margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;'>Complaint Status Update</p>"
                + "</td></tr>"

                // Body
                + "<tr><td style='padding:40px;'>"
                + "<p style='color:#0f172a;font-size:16px;margin:0 0 24px;'>Hi <strong>" + escHtml(residentName) + "</strong>,</p>"
                + "<p style='color:#475569;font-size:15px;margin:0 0 28px;'>Your complaint has been updated by the management. Here are the details:</p>"

                // Status badge
                + "<div style='text-align:center;margin-bottom:28px;'>"
                + "<span style='display:inline-block;padding:10px 28px;border-radius:50px;background:" + statusColor + ";"
                + "color:#ffffff;font-weight:700;font-size:16px;letter-spacing:0.5px;'>" + statusLabel + "</span>"
                + "</div>"

                // Details table
                + "<table width='100%' cellpadding='0' cellspacing='0' style='border-top:1px solid #e2e8f0;'>"
                + "<tr><td style='padding:8px 0;color:#64748b;font-weight:600;'>Complaint</td>"
                + "<td style='padding:8px 0;color:#0f172a;'>" + escHtml(complaint.getTitle()) + "</td></tr>"
                + "<tr><td style='padding:8px 0;color:#64748b;font-weight:600;'>Category</td>"
                + "<td style='padding:8px 0;color:#0f172a;'>" + escHtml(complaint.getCategory()) + "</td></tr>"
                + "<tr><td style='padding:8px 0;color:#64748b;font-weight:600;'>Priority</td>"
                + "<td style='padding:8px 0;'><span style='background:" + priorityColor + ";color:#fff;"
                + "padding:2px 12px;border-radius:20px;font-size:13px;font-weight:600;'>"
                + complaint.getPriority() + "</span></td></tr>"
                + resolutionSection
                + adminNoteSection
                + "</table>"

                + "<p style='color:#475569;font-size:14px;margin:28px 0 0;'>If you have any questions, please contact the hostel management.</p>"
                + "<p style='color:#94a3b8;font-size:13px;margin:12px 0 0;'>— HostelHub Management</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;'>"
                + "<p style='color:#94a3b8;font-size:12px;margin:0;'>This is an automated notification from HostelHub.</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String buildReminderHtml(String residentName, Complaint complaint) {
        String priorityColor = priorityColor(complaint.getPriority());
        String priorityBg = priorityBg(complaint.getPriority());
        String priorityLabel = complaint.getPriority().name();
        String statusLabel = statusLabel(complaint.getStatus());
        String statusColor = statusColor(complaint.getStatus());
        int reminderCount = complaint.getReminderCount() == null ? 1 : complaint.getReminderCount();

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;"
                + "background:#f1f5f9;font-family:Inter,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;"
                + "overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>"

                // Header with priority color
                + "<tr><td style='background:" + priorityBg + ";padding:32px 40px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:700;'>🏠 HostelHub</h1>"
                + "<p style='margin:8px 0 0;color:rgba(255,255,255,0.9);font-size:14px;font-weight:600;'>"
                + "⚠️ Complaint Reminder — " + priorityLabel + " PRIORITY</p>"
                + "</td></tr>"

                // Priority banner
                + "<tr><td style='background:" + priorityColor + ";padding:12px 40px;text-align:center;'>"
                + "<p style='margin:0;color:#ffffff;font-size:15px;font-weight:700;letter-spacing:1px;'>"
                + "REMINDER #" + reminderCount + "</p>"
                + "</td></tr>"

                // Body
                + "<tr><td style='padding:40px;'>"
                + "<p style='color:#0f172a;font-size:16px;margin:0 0 8px;'>Dear Admin,</p>"
                + "<p style='color:#475569;font-size:15px;margin:0 0 28px;'>Resident <strong>" + escHtml(residentName)
                + "</strong> has sent a reminder for their complaint that is still pending action.</p>"

                // Details table
                + "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e2e8f0;"
                + "border-radius:8px;overflow:hidden;margin-bottom:24px;'>"
                + "<tr style='background:#f8fafc;'><td colspan='2' style='padding:12px 16px;font-weight:700;"
                + "color:#0f172a;font-size:14px;'>Complaint Details</td></tr>"
                + "<tr><td style='padding:10px 16px;color:#64748b;font-weight:600;width:38%;border-top:1px solid #e2e8f0;'>Title</td>"
                + "<td style='padding:10px 16px;color:#0f172a;border-top:1px solid #e2e8f0;'><strong>" + escHtml(complaint.getTitle()) + "</strong></td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Category</td>"
                + "<td style='padding:10px 16px;color:#0f172a;'>" + escHtml(complaint.getCategory()) + "</td></tr>"
                + "<tr><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Priority</td>"
                + "<td style='padding:10px 16px;'><span style='background:" + priorityColor + ";color:#fff;"
                + "padding:3px 14px;border-radius:20px;font-size:13px;font-weight:700;'>" + priorityLabel + "</span></td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Current Status</td>"
                + "<td style='padding:10px 16px;'><span style='background:" + statusColor + ";color:#fff;"
                + "padding:3px 14px;border-radius:20px;font-size:13px;font-weight:600;'>" + statusLabel + "</span></td></tr>"
                + "<tr><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Resident</td>"
                + "<td style='padding:10px 16px;color:#0f172a;'>" + escHtml(residentName) + "</td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Reminder Count</td>"
                + "<td style='padding:10px 16px;color:#0f172a;font-weight:700;'>#" + reminderCount + "</td></tr>"
                + "<tr><td style='padding:10px 16px;color:#64748b;font-weight:600;'>Filed On</td>"
                + "<td style='padding:10px 16px;color:#0f172a;'>"
                + (complaint.getComplaintDate() != null ? complaint.getComplaintDate().toString() : "—") + "</td></tr>"
                + "</table>"

                + "<p style='color:#475569;font-size:14px;margin:0 0 8px;'>Description:</p>"
                + "<p style='color:#0f172a;font-size:14px;margin:0 0 28px;background:#f8fafc;padding:14px 16px;"
                + "border-left:4px solid " + priorityColor + ";border-radius:4px;'>"
                + escHtml(complaint.getDescription()) + "</p>"

                + "<p style='color:#ef4444;font-size:14px;font-weight:600;margin:0;'>⏰ Please review this complaint and take appropriate action as soon as possible.</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;'>"
                + "<p style='color:#94a3b8;font-size:12px;margin:0;'>HostelHub Management System — Admin Notification</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendHtml(String toEmail, String subject, String html, String context) {
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[DEV] Would send " + context + " email to: " + toEmail);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
        } catch (Exception e) {
            System.err.println("Failed to send " + context + " email to " + toEmail + ": " + e.getMessage());
        }
    }

    private String priorityColor(ComplaintPriority p) {
        return switch (p) {
            case HIGH   -> "#ef4444";
            case MEDIUM -> "#f59e0b";
            case LOW    -> "#22c55e";
        };
    }

    private String priorityBg(ComplaintPriority p) {
        return switch (p) {
            case HIGH   -> "linear-gradient(135deg,#ef4444,#dc2626)";
            case MEDIUM -> "linear-gradient(135deg,#f59e0b,#d97706)";
            case LOW    -> "linear-gradient(135deg,#22c55e,#16a34a)";
        };
    }

    private String statusColor(ComplaintStatus s) {
        return switch (s) {
            case PENDING         -> "#94a3b8";
            case IN_PROGRESS     -> "#3b82f6";
            case WILL_TAKE_ACTION-> "#8b5cf6";
            case RESOLVED        -> "#22c55e";
            case REJECTED        -> "#ef4444";
        };
    }

    private String statusLabel(ComplaintStatus s) {
        return switch (s) {
            case PENDING          -> "Pending";
            case IN_PROGRESS      -> "In Progress";
            case WILL_TAKE_ACTION -> "Will Take Action";
            case RESOLVED         -> "Resolved ✓";
            case REJECTED         -> "Rejected";
        };
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── Cleaning Schedule Notification → Resident ─────────────────────────────

    @Override
    public void sendCleaningScheduleNotification(String toEmail, String residentName,
                                                  com.hostel.management.entity.RoomCleaningSchedule schedule) {
        if (toEmail == null || toEmail.isBlank()) return;

        String subject = "Room Cleaning Assigned - HostelHub";
        String html = buildCleaningScheduleHtml(residentName, schedule);
        sendHtml(toEmail, subject, html, "cleaning schedule notification");
    }

    private String buildCleaningScheduleHtml(String residentName,
                                              com.hostel.management.entity.RoomCleaningSchedule schedule) {
        String roomNumber = schedule.getRoom() != null ? schedule.getRoom().getRoomNumber() : "N/A";
        String scheduledDate = schedule.getScheduledDate() != null ? schedule.getScheduledDate().toString() : "N/A";
        String assigned = schedule.getAssignedResidents() != null ? escHtml(schedule.getAssignedResidents()) : "All room residents";
        String notes = schedule.getNotes() != null && !schedule.getNotes().isBlank()
                ? escHtml(schedule.getNotes()) : "None";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;"
                + "background:#f1f5f9;font-family:Inter,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;"
                + "overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#06b6d4,#6366f1);padding:32px 40px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:700;'>🏠 HostelHub</h1>"
                + "<p style='margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;'>Room Cleaning Assignment</p>"
                + "</td></tr>"

                // Body
                + "<tr><td style='padding:40px;'>"
                + "<p style='color:#0f172a;font-size:16px;margin:0 0 24px;'>Hi <strong>" + escHtml(residentName) + "</strong>,</p>"
                + "<p style='color:#475569;font-size:15px;margin:0 0 28px;'>A room cleaning task has been assigned to your room. Please make sure your room is cleaned by the scheduled date.</p>"

                // Info badge
                + "<div style='text-align:center;margin-bottom:28px;'>"
                + "<span style='display:inline-block;padding:10px 28px;border-radius:50px;background:#06b6d4;"
                + "color:#ffffff;font-weight:700;font-size:16px;letter-spacing:0.5px;'>🧹 Cleaning Scheduled</span>"
                + "</div>"

                // Details table
                + "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;'>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;width:40%;'>Room</td>"
                + "<td style='padding:12px 16px;color:#0f172a;font-weight:700;'>" + escHtml(roomNumber) + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#64748b;font-weight:600;border-top:1px solid #e2e8f0;'>Scheduled Date</td>"
                + "<td style='padding:12px 16px;color:#0f172a;font-weight:700;border-top:1px solid #e2e8f0;'>" + scheduledDate + "</td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;'>Week</td>"
                + "<td style='padding:12px 16px;color:#0f172a;'>Week " + schedule.getWeekNumber() + ", " + schedule.getYear() + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#64748b;font-weight:600;border-top:1px solid #e2e8f0;'>Assigned To</td>"
                + "<td style='padding:12px 16px;color:#0f172a;border-top:1px solid #e2e8f0;'>" + assigned + "</td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;'>Notes</td>"
                + "<td style='padding:12px 16px;color:#0f172a;'>" + notes + "</td></tr>"
                + "</table>"

                + "<p style='color:#475569;font-size:14px;margin:28px 0 0;'>You can mark it as cleaned from your Resident Dashboard once done.</p>"
                + "<p style='color:#94a3b8;font-size:13px;margin:12px 0 0;'>— HostelHub Management</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;'>"
                + "<p style='color:#94a3b8;font-size:12px;margin:0;'>This is an automated notification from HostelHub.</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private void logVerificationLink(String toEmail, String link) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 [DEV] Verification email for " + toEmail);
        System.out.println("   → Open this link to verify: " + link);
        System.out.println("   Configure spring.mail.* to send real emails.");
        System.out.println("═══════════════════════════════════════════════════════");
    }

    // ── Cleaning Task Notification ────────────────────────────────────────────

    @Override
    public void sendCleaningTaskNotification(String toEmail, String residentName,
                                              com.hostel.management.entity.CleaningTask task) {
        if (toEmail == null || toEmail.isBlank()) return;

        String subject = "Cleaning Task Assigned - HostelHub";
        String html = buildCleaningTaskHtml(residentName, task);
        sendHtml(toEmail, subject, html, "cleaning task notification");
    }

    private String buildCleaningTaskHtml(String residentName,
                                          com.hostel.management.entity.CleaningTask task) {
        String area = task.getArea() != null ? escHtml(task.getArea()) : "N/A";
        String day = task.getDayOfWeek() != null ? escHtml(task.getDayOfWeek()) : "N/A";
        String time = task.getTimeSlot() != null ? escHtml(task.getTimeSlot()) : "N/A";
        String dateStr = task.getScheduledDate() != null ? task.getScheduledDate().toString() : "N/A";
        String notes = task.getNotes() != null && !task.getNotes().isBlank()
                ? escHtml(task.getNotes()) : "None";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;"
                + "background:#f1f5f9;font-family:Inter,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;"
                + "overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>"
                + "<tr><td style='background:linear-gradient(135deg,#06b6d4,#6366f1);padding:32px 40px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:700;'>🏠 HostelHub</h1>"
                + "<p style='margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;'>Cleaning Task Assignment</p>"
                + "</td></tr>"
                + "<tr><td style='padding:40px;'>"
                + "<p style='color:#0f172a;font-size:16px;margin:0 0 24px;'>Hi <strong>" + escHtml(residentName) + "</strong>,</p>"
                + "<p style='color:#475569;font-size:15px;margin:0 0 28px;'>A cleaning task has been assigned to you. Please complete it on time.</p>"
                + "<div style='text-align:center;margin-bottom:28px;'>"
                + "<span style='display:inline-block;padding:10px 28px;border-radius:50px;background:#06b6d4;"
                + "color:#ffffff;font-weight:700;font-size:16px;letter-spacing:0.5px;'>🧹 Cleaning Assigned</span></div>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e2e8f0;border-radius:8px;overflow:hidden;'>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;width:40%;'>Area</td>"
                + "<td style='padding:12px 16px;color:#0f172a;font-weight:700;'>" + area + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#64748b;font-weight:600;border-top:1px solid #e2e8f0;'>Day</td>"
                + "<td style='padding:12px 16px;color:#0f172a;border-top:1px solid #e2e8f0;'>" + day + "</td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;'>Time</td>"
                + "<td style='padding:12px 16px;color:#0f172a;'>" + time + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#64748b;font-weight:600;border-top:1px solid #e2e8f0;'>Scheduled Date</td>"
                + "<td style='padding:12px 16px;color:#0f172a;font-weight:700;border-top:1px solid #e2e8f0;'>" + dateStr + "</td></tr>"
                + "<tr style='background:#f8fafc;'><td style='padding:12px 16px;color:#64748b;font-weight:600;'>Notes</td>"
                + "<td style='padding:12px 16px;color:#0f172a;'>" + notes + "</td></tr>"
                + "</table>"
                + "<p style='color:#475569;font-size:14px;margin:28px 0 0;'>You can mark it as completed from your Resident Dashboard.</p>"
                + "<p style='color:#94a3b8;font-size:13px;margin:12px 0 0;'>— HostelHub Management</p>"
                + "</td></tr>"
                + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;'>"
                + "<p style='color:#94a3b8;font-size:12px;margin:0;'>This is an automated notification from HostelHub.</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }
}
