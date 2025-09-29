package com.music.musicstore.api;

import com.music.musicstore.models.order.Order;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailTestController {

    @Autowired
    private EmailSender emailSender;

    @PostMapping("/send-test-email")
    public ResponseEntity<?> sendTestEmail(@RequestParam String email) {
        try {
            // Create a test receipt email
            StringBuilder itemList = new StringBuilder();
            itemList.append("Test Music Track 1 - $9.99\n");
            itemList.append("Test Music Track 2 - $12.99\n");
            itemList.append("Test Music Track 3 - $7.99\n");

            BigDecimal totalAmount = new BigDecimal("30.97");
            String orderId = "TEST-" + System.currentTimeMillis();

            emailSender.sendReceipt(totalAmount, itemList, email, orderId);

            return ResponseEntity.ok().body(Map.of(
                "message", "Test email sent successfully!",
                "sentTo", email,
                "orderId", orderId,
                "totalAmount", totalAmount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to send email: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/send-simple-test")
    public ResponseEntity<?> sendSimpleTestEmail(@RequestParam String email) {
        try {
            String subject = "Music Store - Email Test";
            String body = "Hello!\n\n" +
                         "This is a test email to verify that the Music Store email functionality is working correctly.\n\n" +
                         "If you received this email, the email configuration is working properly!\n\n" +
                         "Best regards,\n" +
                         "Music Store Team";

            emailSender.sendReceiptEmail(email, subject, body);

            return ResponseEntity.ok().body(Map.of(
                "message", "Simple test email sent successfully!",
                "sentTo", email
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to send email: " + e.getMessage()
            ));
        }
    }
}
