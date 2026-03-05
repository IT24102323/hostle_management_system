package com.hostel.management.service;

import com.hostel.management.entity.CleaningTask;
import com.hostel.management.entity.Complaint;
import com.hostel.management.entity.RoomCleaningSchedule;

/**
 * Sends verification and other emails.
 */
public interface EmailService {

    void sendVerificationEmail(String toEmail, String name, String verificationToken);

    void sendComplaintStatusUpdateEmail(Complaint complaint);

    void sendReminderEmailToAdmin(Complaint complaint);

    void sendCleaningScheduleNotification(String toEmail, String residentName, RoomCleaningSchedule schedule);

    void sendCleaningTaskNotification(String toEmail, String residentName, CleaningTask task);
}
