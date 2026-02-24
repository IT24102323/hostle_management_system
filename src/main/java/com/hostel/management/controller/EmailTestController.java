package com.hostel.management.controller;

import com.hostel.management.service.EmailService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing email functionality.
 * Remove this in production or secure it properly.
 */
@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Test endpoint to send a verification email.
     * Usage: POST /api/test/send-email?email=test@example.com&name=TestUser
     */
    @PostMapping("/send-email")
    public String testSendEmail(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Test User") String name) {

        try {
            // Generate a test token
            String testToken = "test-token-" + System.currentTimeMillis();

            emailService.sendVerificationEmail(email, name, testToken);

            return "✅ Email sending attempted for: " + email +
                   "\nCheck server logs for success/failure details." +
                   "\nIf email configuration is not set up, check the console for the verification link.";
        } catch (Exception e) {
            return "❌ Error sending email: " + e.getMessage();
        }
    }

    /**
     * Health check for email service.
     */
    @GetMapping("/email-status")
    public String getEmailStatus() {
        return "Email service is available. Use POST /api/test/send-email?email=your@email.com to test.";
    }
}
